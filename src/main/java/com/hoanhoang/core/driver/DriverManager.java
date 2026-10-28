package com.hoanhoang.core.driver;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import java.util.Locale;
import org.openqa.selenium.WebDriver;

public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {}

    public static void setDriver(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver must not be null");
        }
        if (DRIVER.get() != null) {
            throw new IllegalStateException(
                    "A WebDriver is already registered for thread: "
                            + Thread.currentThread().getName());
        }
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No WebDriver is registered for thread: " + Thread.currentThread().getName());
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static WebDriver startDriver() {
        EnvironmentConfig config = ConfigLoader.getConfig();
        return startDriver(
                BrowserType.valueOf(config.getBrowser().trim().toUpperCase(Locale.ROOT)),
                config.isHeadless());
    }

    public static WebDriver startDriver(BrowserType browserType, boolean headless) {
        if (hasDriver()) {
            throw new IllegalStateException(
                    "A WebDriver is already registered for thread: "
                            + Thread.currentThread().getName());
        }
        WebDriver driver = DriverFactory.createDriver(browserType, headless);
        setDriver(driver);
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER.remove();
        }
    }
}
