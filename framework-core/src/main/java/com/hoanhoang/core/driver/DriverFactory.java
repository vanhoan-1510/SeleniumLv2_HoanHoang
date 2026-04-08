package com.hoanhoang.core.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

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
        applyCommonArguments(options, headless);
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) options.addArguments("-headless");
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        applyCommonArguments(options, headless);
        return new EdgeDriver(options);
    }

    private static String[] getCommonArguments(boolean headless) {
        java.util.List<String> args = new java.util.ArrayList<>();
        if (headless) args.add("--headless");
        args.add("--no-sandbox");
        args.add("--disable-dev-shm-usage");
        return args.toArray(new String[0]);
    }

    private static void applyCommonArguments(ChromeOptions options, boolean headless) {
        for (String arg : getCommonArguments(headless)) {
            options.addArguments(arg);
        }
    }

    private static void applyCommonArguments(EdgeOptions options, boolean headless) {
        for (String arg : getCommonArguments(headless)) {
            options.addArguments(arg);
        }
    }
}