package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.RetryHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.Locale;

public class BaseElement implements Element {

    protected By locator;
    protected WebElement element;

    public BaseElement(By locator) {
        this.locator = locator;
    }

    public BaseElement(WebElement element) {
        this.element = element;
    }

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    private static EnvironmentConfig config() {
        return ConfigLoader.getConfig();
    }

    private WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(
                driver(),
                Duration.ofSeconds(timeoutSeconds),
                Duration.ofMillis(config().getPollingInterval())
        );
    }

    private WebDriverWait getElementWait() {
        return getWait(config().getElementTimeout());
    }

    private WebDriverWait getActionWait() {
        return getWait(config().getActionTimeout());
    }

    private WebDriverWait getAssertionWait() {
        return getWait(config().getAssertionTimeout());
    }

    protected WebElement getElement() {
        if (element != null) {
            return element;
        }
        return getElementWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement getVisibleElement() {
        if (element != null) {
            if (!element.isDisplayed()) {
                throw new AssertionError("Element is not visible");
            }
            return element;
        }
        return getElementWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement getClickableElement() {
        if (element != null) {
            if (!element.isDisplayed() || !element.isEnabled()) {
                throw new AssertionError("Element is not clickable");
            }
            return element;
        }
        return getActionWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    private String getTextFrom(WebElement el) {
        String text = el.getText();
        if (text != null && !text.trim().isEmpty()) {
            return text.trim();
        }

        Object jsText = ((JavascriptExecutor) driver()).executeScript(
                "return (arguments[0].innerText || arguments[0].textContent || '').trim();",
                el
        );
        return jsText == null ? "" : jsText.toString();
    }

    public static int count(String xpath) {
        return driver().findElements(By.xpath(xpath)).size();
    }

    @Override
    public void click() {
        retryAction(() -> {
            WebElement el = getClickableElement();
            ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
            el.click();
        });
    }

    @Override
    public void jsClick() {
        retryAction(() -> {
            WebElement el = getElement();
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", el);
        });
    }

    @Override
    public void type(String text) {
        retryAction(() -> {
            WebElement el = getVisibleElement();
            el.clear();
            el.sendKeys(text);
        });
    }

    @Override
    public void shouldHaveText(String expected) {
        retryAssert(() -> {
            String actual = getTextFrom(getVisibleElement());
            Assert.assertEquals(actual, expected,
                    "Text mismatch. Expected: '" + expected + "', Actual: '" + actual + "'");
        });
    }

    @Override
    public void shouldContainText(String expected) {
        retryAssert(() -> {
            String actual = getTextFrom(getVisibleElement());
            Assert.assertTrue(actual.contains(expected),
                    "Text should contain: '" + expected + "', Actual: '" + actual + "'");
        });
    }

    @Override
    public void shouldContainTextIgnoringCase(String expected) {
        retryAssert(() -> {
            String actual = getTextFrom(getVisibleElement());
            Assert.assertTrue(
                    actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT)),
                    "Text should contain ignoring case: '" + expected + "', Actual: '" + actual + "'"
            );
        });
    }

    @Override
    public void shouldHaveValue(String expected) {
        retryAssert(() -> {
            String actual = getClickableElement().getAttribute("value");
            Assert.assertEquals(actual, expected,
                    "Value mismatch. Expected: '" + expected + "', Actual: '" + actual + "'");
        });
    }

    @Override
    public void shouldHave(String prop, String expected) {
        retryAssert(() -> {
            String actual = getVisibleElement().getAttribute(prop);
            Assert.assertEquals(actual, expected,
                    "Attribute '" + prop + "' mismatch. Expected: '" + expected + "', Actual: '" + actual + "'");
        });
    }

    @Override
    public String getText() {
        return getTextFrom(getVisibleElement());
    }

    @Override
    public String getValue() {
        return getClickableElement().getAttribute("value");
    }

    @Override
    public void hover() {
        retryAction(() -> {
            WebElement el = getVisibleElement();
            new Actions(driver())
                    .moveToElement(el)
                    .pause(Duration.ofMillis(500))
                    .perform();
        });
    }

    @Override
    public boolean isVisible() {
        if (element != null) {
            try {
                return element.isDisplayed();
            } catch (Exception e) {
                return false;
            }
        }
        try {
            getAssertionWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void shouldBeVisible() {
        retryAssert(() -> {
            boolean visible = isVisible();
            Assert.assertTrue(visible, "Element is NOT visible: " + locator.toString());
        });
    }

    private void retryAssert(Runnable assertion) {
        RetryHandler.retryUntilTimeout(assertion, config().getStepRetryTimeoutMs(), config().getStepRetryIntervalMs());
    }

    private void retryAction(Runnable action) {
        RetryHandler.retryUntilTimeout(action, config().getStepRetryTimeoutMs(), config().getStepRetryIntervalMs());
    }
}
