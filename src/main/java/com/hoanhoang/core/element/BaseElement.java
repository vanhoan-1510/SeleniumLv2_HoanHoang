package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.UiWait;
import java.util.Objects;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class BaseElement implements Element {
    private final Function<WebDriver, WebElement> resolver;
    private final String description;

    public BaseElement(By locator) {
        Objects.requireNonNull(locator);
        this.resolver =
                d ->
                        d.findElements(locator).stream()
                                .filter(WebElement::isDisplayed)
                                .findFirst()
                                .orElse(null);
        this.description = locator.toString();
    }

    public BaseElement(WebElement element) {
        Objects.requireNonNull(element);
        this.resolver = d -> element;
        this.description = "Wrapped element";
    }

    BaseElement(Function<WebDriver, WebElement> resolver, String description) {
        this.resolver = Objects.requireNonNull(resolver);
        this.description = description;
    }

    private WebElement resolveVisible(WebDriver driver) {
        WebElement current = resolver.apply(driver);
        return current != null && current.isDisplayed() ? current : null;
    }

    private WebElement visible() {
        return UiWait.until(
                description + " visible",
                ConfigLoader.getConfig().getElementTimeout() * 1000L,
                this::resolveVisible);
    }

    private WebElement clickable() {
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
        UiWait.click(description, resolver);
    }

    @Override
    public void jsClick() {
        ((JavascriptExecutor) DriverManager.getDriver())
                .executeScript("arguments[0].click();", clickable());
    }

    @Override
    public void type(String text) {
        WebElement input = clickable();
        input.clear();
        input.sendKeys(text);
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
    public void hover() {
        new Actions(DriverManager.getDriver()).moveToElement(visible()).perform();
    }

    @Override
    public boolean isVisible() {
        try {
            return resolveVisible(DriverManager.getDriver()) != null;
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    @Override
    public Element shouldHave(Condition condition) {
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
