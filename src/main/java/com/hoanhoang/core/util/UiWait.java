package com.hoanhoang.core.util;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.report.logger.Log;
import io.qameta.allure.Allure;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
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
                                        + (last[0] == null ? "<not visible>" : "'" + last[0] + "'"),
                                e);
                    }
                });
    }
}
