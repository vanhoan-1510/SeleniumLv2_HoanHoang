package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class BaseElement implements Element {

    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 2;
    private static final int DEFAULT_RETRY_INTERVAL_MS = 500;

    protected By locator;
    protected WebElement element;

    public BaseElement(By locator) {
        this.locator = locator;
    }

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    private static EnvironmentConfig config() {
        return ConfigLoader.getConfig();
    }

    private static int getMaxRetryAttempts() {
        return config().getMaxRetryAttempts() > 0 ? config().getMaxRetryAttempts() : DEFAULT_MAX_RETRY_ATTEMPTS;
    }

    private static int getRetryIntervalMs() {
        return config().getRetryIntervalMs() > 0 ? config().getRetryIntervalMs() : DEFAULT_RETRY_INTERVAL_MS;
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
        return getElementWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement getVisibleElement() {
        return getElementWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement getClickableElement() {
        return getActionWait().until(ExpectedConditions.elementToBeClickable(locator));
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
            String actual = getVisibleElement().getText();
            Assert.assertEquals(actual, expected,
                    "Text mismatch. Expected: '" + expected + "', Actual: '" + actual + "'");
        });
    }

    @Override
    public void shouldContainText(String expected) {
        retryAssert(() -> {
            String actual = getVisibleElement().getText();
            Assert.assertTrue(actual.contains(expected),
                    "Text should contain: '" + expected + "', Actual: '" + actual + "'");
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
        return getVisibleElement().getText();
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
        try {
            getAssertionWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void shouldBeVisible() {
        boolean visible = isVisible();
        Assert.assertTrue(visible, "Element is NOT visible: " + locator.toString());
    }

    private void retryAssert(Runnable assertion) {
        AssertionError lastError = null;
        int maxAttempts = getMaxRetryAttempts();
        int intervalMs = getRetryIntervalMs();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                assertion.run();
                return;
            } catch (AssertionError e) {
                lastError = e;
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(intervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during retry", ie);
                    }
                }
            }
        }
        throw lastError;
    }

    private void retryAction(Runnable action) {
        RuntimeException lastError = null;
        int maxAttempts = getMaxRetryAttempts();
        int intervalMs = getRetryIntervalMs();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                lastError = (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(intervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during retry", ie);
                    }
                }
            }
        }
        throw lastError;
    }
}