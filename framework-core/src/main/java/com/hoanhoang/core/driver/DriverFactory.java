package com.hoanhoang.core.driver;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import com.hoanhoang.core.exception.DriverInitializationException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class DriverFactory {

    public static WebDriver createDriver(BrowserType browserType, boolean headless) {
        WebDriver driver = createWebDriver(browserType, headless);
        configureDriver(driver, headless);
        return driver;
    }

    private static WebDriver createWebDriver(BrowserType browserType, boolean headless) {
        Capabilities options = createOptions(browserType, headless);
        if (isRemote()) {
            return createRemoteDriver(options);
        }

        switch (browserType) {
            case CHROME:
                return new ChromeDriver((ChromeOptions) options);
            case FIREFOX:
                return new FirefoxDriver((FirefoxOptions) options);
            case EDGE:
                return new EdgeDriver((EdgeOptions) options);
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserType);
        }
    }

    private static Capabilities createOptions(BrowserType browserType, boolean headless) {
        switch (browserType) {
            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                applyCommonArguments(chromeOptions, headless);
                return chromeOptions;
            case FIREFOX:
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                applyFirefoxArguments(firefoxOptions, headless);
                return firefoxOptions;
            case EDGE:
                EdgeOptions edgeOptions = new EdgeOptions();
                applyCommonArguments(edgeOptions, headless);
                return edgeOptions;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserType);
        }
    }

    private static WebDriver createRemoteDriver(Capabilities options) {
        try {
            return new RemoteWebDriver(new URL(getGridUrl()), options);
        } catch (MalformedURLException e) {
            throw new DriverInitializationException("Invalid Selenium Grid URL: " + getGridUrl(), e);
        }
    }

    private static void configureDriver(WebDriver driver, boolean headless) {
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config().getNavigationTimeout()));
        if (headless) {
            driver.manage().window().setSize(new Dimension(getWindowWidth(), getWindowHeight()));
        } else {
            try {
                driver.manage().window().maximize();
            } catch (RuntimeException ignored) {
                driver.manage().window().setSize(new Dimension(getWindowWidth(), getWindowHeight()));
            }
        }
    }

    private static String[] getCommonArguments(boolean headless) {
        java.util.List<String> args = new java.util.ArrayList<>();
        if (headless) {
            args.add("--headless=new");
            args.add("--window-size=" + getWindowWidth() + "," + getWindowHeight());
            args.add("--force-device-scale-factor=1");
        }
        args.add("--no-sandbox");
        args.add("--disable-dev-shm-usage");
        return args.toArray(new String[0]);
    }

    private static int getWindowWidth() {
        return Integer.parseInt(System.getProperty(
                "windowWidth",
                String.valueOf(config().getWindowWidth())
        ));
    }

    private static int getWindowHeight() {
        return Integer.parseInt(System.getProperty(
                "windowHeight",
                String.valueOf(config().getWindowHeight())
        ));
    }

    private static boolean isRemote() {
        return Boolean.parseBoolean(System.getProperty(
                "remote",
                String.valueOf(config().isRemote())
        ));
    }

    private static String getGridUrl() {
        String gridUrl = System.getProperty("gridUrl", config().getGridUrl());
        if (gridUrl == null || gridUrl.trim().isEmpty()) {
            throw new DriverInitializationException("gridUrl must be provided when remote=true");
        }
        return gridUrl;
    }

    private static EnvironmentConfig config() {
        return ConfigLoader.getConfig();
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

    private static void applyFirefoxArguments(FirefoxOptions options, boolean headless) {
        if (headless) {
            options.addArguments(
                    "-headless",
                    "--width=" + getWindowWidth(),
                    "--height=" + getWindowHeight()
            );
        }
    }
}
