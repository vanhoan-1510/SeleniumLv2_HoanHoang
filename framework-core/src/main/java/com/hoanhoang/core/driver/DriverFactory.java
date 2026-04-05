package com.hoanhoang.core.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class DriverFactory {

    public static WebDriver createDriver(BrowserType browserType, boolean headless) {
        switch (browserType) {
            case CHROME:
                return createChromeDriver(headless);
            case FIREFOX:
                return createFirefoxDriver(headless);
            case EDGE:
                return createEdgeDriver(headless);
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserType);
        }
    }

    private static WebDriver createChromeDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        // TODO: add Firefox options
        return new FirefoxDriver();
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        // TODO: add Edge options
        return new EdgeDriver();
    }
}