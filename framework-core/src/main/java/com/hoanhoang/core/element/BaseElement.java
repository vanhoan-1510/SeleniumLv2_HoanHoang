package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
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

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_INTERVAL_MS = 1000;

    protected By locator;
    protected WebElement element;

    public BaseElement(By locator) {
        this.locator = locator;
    }

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    protected WebElement getElement() {
        WebElement el = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        if (!el.isDisplayed()) {
            el = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
        return el;
    }

    protected WebElement getClickableElement() {
        WebElement el = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        if (!el.isDisplayed() || !el.isEnabled()) {
            el = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            el = getWait().until(ExpectedConditions.elementToBeClickable(locator));
        }
        return el;
    }

    private WebDriverWait getWait() {
        return new WebDriverWait(
                driver(),
                Duration.ofSeconds(ConfigLoader.getConfig().getTimeout())
        );
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
            WebElement el = getElement();
            el.clear();
            el.sendKeys(text);
        });
    }

    @Override
    public void shouldHaveText(String expected) {
        retryAssert(() -> {
            String actual = getElement().getText();
            Assert.assertEquals(actual, expected,
                    "Text mismatch. Expected: '" + expected + "', Actual: '" + actual + "'");
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
            String actual = getElement().getAttribute(prop);
            Assert.assertEquals(actual, expected,
                    "Attribute '" + prop + "' mismatch. Expected: '" + expected + "', Actual: '" + actual + "'");
        });
    }

    @Override
    public String getText() {
        return getElement().getText();
    }

    @Override
    public String getValue() {
        return getClickableElement().getAttribute("value");
    }

    @Override
    public void hover() {
        retryAction(() -> {
            WebElement el = getElement();
            new Actions(driver())
                    .moveToElement(el)
                    .pause(Duration.ofMillis(500))
                    .perform();
        });
    }

    @Override
    public boolean isVisible() {
        try {
            getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void shouldBeVisible() {
        retryAssert(() -> {
            boolean visible = false;
            try {
                getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
                visible = true;
            } catch (Exception e) {
            }
            Assert.assertTrue(visible, "Element is NOT visible: " + locator.toString());
        });
    }

    private void retryAssert(Runnable assertion) {
        AssertionError lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                assertion.run();
                return;
            } catch (AssertionError e) {
                lastError = e;
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
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
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                lastError = (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
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
