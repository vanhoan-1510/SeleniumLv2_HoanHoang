package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.JavaScript;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.report.logger.Log;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

public class BaseElement implements Element {
    private final Function<WebDriver, WebElement> resolver;
    private final String description;
    private final boolean snapshot;

    public BaseElement(By locator) {
        Objects.requireNonNull(locator);
        this.resolver = driver -> firstMatch(driver.findElements(locator));
        this.description = locator.toString();
        this.snapshot = false;
    }

    BaseElement(Function<WebDriver, WebElement> resolver, String description, boolean snapshot) {
        this.resolver = Objects.requireNonNull(resolver);
        this.description = description;
        this.snapshot = snapshot;
    }

    static Element snapshot(WebElement element, String description) {
        return new BaseElement(driver -> element, description, true);
    }

    private static WebElement firstMatch(List<WebElement> matches) {
        return matches.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseGet(() -> matches.isEmpty() ? null : matches.getFirst());
    }

    private WebElement requireResolved() {
        WebElement element = resolver.apply(DriverManager.getDriver());
        if (element == null) {
            throw new NoSuchElementException("Element not found: " + description);
        }
        return element;
    }

    private WebElement present() {
        return snapshot
                ? requireResolved()
                : UiWait.until(
                        description + " present",
                        ConfigLoader.getConfig().getElementTimeout() * 1000L,
                        resolver);
    }

    private void requireLiveElement() {
        if (snapshot) {
            throw new IllegalStateException(
                    "Snapshot callbacks support reads only: " + description);
        }
    }

    private WebElement resolveVisible(WebDriver driver) {
        WebElement current = resolver.apply(driver);
        return current != null && current.isDisplayed() ? current : null;
    }

    private WebElement visible() {
        if (snapshot) {
            return requireResolved();
        }
        return UiWait.until(
                description + " visible",
                ConfigLoader.getConfig().getElementTimeout() * 1000L,
                this::resolveVisible);
    }

    private WebElement clickable() {
        requireLiveElement();
        return UiWait.until(
                description + " clickable",
                ConfigLoader.getConfig().getActionTimeout() * 1000L,
                d -> {
                    WebElement current = resolveVisible(d);
                    return current != null && current.isEnabled() ? current : null;
                });
    }

    @Override
    public void click() {
        requireLiveElement();
        Log.debug("Click: " + description);
        UiWait.until(
                "Click " + description,
                ConfigLoader.getConfig().getActionTimeout() * 1000L,
                driver -> {
                    WebElement element = resolveVisible(driver);
                    if (element == null || !element.isEnabled()) {
                        return false;
                    }
                    JavaScript.execute("arguments[0].scrollIntoView({block:'center'});", element);
                    element.click();
                    return true;
                });
    }

    @Override
    public void jsClick() {
        JavaScript.execute("arguments[0].click();", clickable());
    }

    @Override
    public void type(String text) {
        WebElement input = clickable();
        input.clear();
        input.sendKeys(text);
    }

    @Override
    public void blur() {
        requireLiveElement();
        blur(visible());
    }

    private static void blur(WebElement input) {
        JavaScript.execute("arguments[0].blur();", input);
    }

    @Override
    public void replaceTextAndBlur(String text) {
        WebElement input = clickable();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(text);
        blur(input);
    }

    @Override
    public void selectByValue(String value) {
        new Select(clickable()).selectByValue(value);
    }

    @Override
    public Element find(By locator) {
        Objects.requireNonNull(locator);
        return new BaseElement(
                driver -> {
                    WebElement parent = resolver.apply(driver);
                    return parent == null ? null : firstMatch(parent.findElements(locator));
                },
                description + " -> " + locator,
                snapshot);
    }

    @Override
    public Elements findAll(By locator) {
        Objects.requireNonNull(locator);
        return new BaseElements(
                driver -> {
                    WebElement parent = resolver.apply(driver);
                    return parent == null ? List.of() : parent.findElements(locator);
                },
                description + " -> " + locator,
                snapshot);
    }

    @Override
    public <T> T readSnapshot(Function<Element, T> reader) {
        return reader.apply(snapshot(requireResolved(), description));
    }

    @Override
    public BooleanSupplier watchForReplacement() {
        WebElement original = visible();
        return () -> {
            try {
                original.isEnabled();
                return false;
            } catch (StaleElementReferenceException e) {
                return true;
            }
        };
    }

    @Override
    public String getText() {
        return visible().getText().trim();
    }

    @Override
    public String getValue() {
        return visible().getDomProperty("value");
    }

    @Override
    public String getDomAttribute(String name) {
        return present().getDomAttribute(name);
    }

    @Override
    public String getDomProperty(String name) {
        return present().getDomProperty(name);
    }

    @Override
    public boolean exists() {
        return readState(element -> element != null);
    }

    @Override
    public boolean isSelected() {
        return readState(element -> element != null && element.isSelected());
    }

    @Override
    public void hover() {
        requireLiveElement();
        new Actions(DriverManager.getDriver()).moveToElement(visible()).perform();
    }

    @Override
    public boolean isVisible() {
        return readState(element -> element != null && element.isDisplayed());
    }

    private boolean readState(Predicate<WebElement> condition) {
        try {
            return condition.test(resolver.apply(DriverManager.getDriver()));
        } catch (StaleElementReferenceException e) {
            if (snapshot) {
                throw e;
            }
            return false;
        }
    }

    @Override
    public Element waitUntilVisible() {
        requireLiveElement();
        visible();
        return this;
    }

    @Override
    public Element waitUntilPresent() {
        waitUntil(description + " present", Element::exists);
        return this;
    }

    @Override
    public Element waitUntilSelected() {
        waitUntil(description + " selected", Element::isSelected);
        return this;
    }

    @Override
    public <T> T waitUntil(String expectation, Function<Element, T> condition) {
        requireLiveElement();
        Objects.requireNonNull(condition);
        return UiWait.until(
                expectation,
                driver -> condition.apply(snapshot(resolver.apply(driver), description)));
    }

    @Override
    public Element shouldHave(Condition condition) {
        requireLiveElement();
        UiWait.assertEventually(
                description + " should have " + condition,
                d -> {
                    WebElement current = resolveVisible(d);
                    return current == null ? null : condition.read(current);
                },
                condition::matches);
        return this;
    }
}
