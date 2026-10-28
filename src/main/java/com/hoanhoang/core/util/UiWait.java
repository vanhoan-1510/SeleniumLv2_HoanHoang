package com.hoanhoang.core.util;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.report.logger.Log;
import io.qameta.allure.Allure;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class UiWait {
    private UiWait() {}

    public static <T> T until(String description, Function<WebDriver, T> condition) {
        return until(description, ConfigLoader.getConfig().getUiTimeoutMs(), condition);
    }

    public static <T> T until(
            String description, long timeoutMs, Function<WebDriver, T> condition) {
        var config = ConfigLoader.getConfig();
        Log.debug("Wait up to " + timeoutMs + "ms: " + description);
        return new WebDriverWait(
                        DriverManager.getDriver(),
                        Duration.ofMillis(timeoutMs),
                        Duration.ofMillis(config.getPollingInterval()))
                .ignoring(StaleElementReferenceException.class)
                .withMessage(description)
                .until(condition);
    }

    /**
     * Auto-retry check: re-reads the actual value until it matches or uiTimeoutMs expires, then
     * fails with expected vs last actual. Recorded as an Allure step so passing checks also appear
     * in the report.
     */
    public static void assertEventually(
            String expectation, Function<WebDriver, String> actual, Predicate<String> matches) {
        Allure.step(
                "Verify " + expectation,
                () -> {
                    String[] last = {null};
                    try {
                        until(
                                expectation,
                                d -> {
                                    last[0] = actual.apply(d);
                                    return matches.test(last[0]);
                                });
                    } catch (TimeoutException e) {
                        throw new AssertionError(
                                expectation
                                        + ", but was "
                                        + (last[0] == null
                                                ? "<not visible>"
                                                : "'" + last[0] + "'"));
                    }
                });
    }

    public static WebElement visible(By locator) {
        return until(
                "Visible element: " + locator,
                ConfigLoader.getConfig().getElementTimeout() * 1000L,
                d ->
                        d.findElements(locator).stream()
                                .filter(WebElement::isDisplayed)
                                .findFirst()
                                .orElse(null));
    }

    public static WebElement clickable(By locator) {
        return until(
                "Clickable element: " + locator,
                ConfigLoader.getConfig().getActionTimeout() * 1000L,
                d ->
                        d.findElements(locator).stream()
                                .filter(e -> e.isDisplayed() && e.isEnabled())
                                .findFirst()
                                .orElse(null));
    }

    public static boolean isVisible(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public static List<WebElement> visibleElements(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    public static void click(By locator) {
        click(
                locator.toString(),
                d ->
                        d.findElements(locator).stream()
                                .filter(WebElement::isDisplayed)
                                .findFirst()
                                .orElse(null));
    }

    /** Re-resolves the target if it becomes stale between readiness and the click command. */
    public static void click(String description, Function<WebDriver, WebElement> resolver) {
        Log.debug("Click: " + description);
        until(
                "Click " + description,
                ConfigLoader.getConfig().getActionTimeout() * 1000L,
                d -> {
                    WebElement element = resolver.apply(d);
                    if (element == null || !element.isDisplayed() || !element.isEnabled()) {
                        return false;
                    }
                    ((JavascriptExecutor) d)
                            .executeScript(
                                    "arguments[0].scrollIntoView({block:'center'});", element);
                    element.click();
                    return true;
                });
    }
}
