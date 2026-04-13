package com.hoanhoang.core.driver;

import org.openqa.selenium.WebDriver;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Set driver
    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    // Get driver
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    // Quit driver
    public static void quitDriver() {
        WebDriver driver = getDriver();
        if (driver != null) {
            driver.quit();
        }
    }

    public static void removeDriver() {
        driverThreadLocal.remove();
    }
}