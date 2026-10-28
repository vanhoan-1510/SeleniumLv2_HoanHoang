package com.hoanhoang.core.driver;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import com.hoanhoang.core.exception.DriverInitializationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver createDriver(BrowserType browserType, boolean headless) {
        EnvironmentConfig config = ConfigLoader.getConfig();
        WebDriver driver = createWebDriver(browserType, headless, config);
        try {
            configureDriver(driver, headless, config);
            return driver;
        } catch (RuntimeException e) {
            try {
                driver.quit();
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw new DriverInitializationException(
                    "Failed to configure browser: " + browserType, e);
        }
    }

    private static WebDriver createWebDriver(
            BrowserType browserType, boolean headless, EnvironmentConfig config) {
        Capabilities options = createOptions(browserType, headless, config);
        if (config.isRemote()) {
            return createRemoteDriver(options, config);
        }

        return switch (browserType) {
            case CHROME -> new ChromeDriver((ChromeOptions) options);
            case FIREFOX -> new FirefoxDriver((FirefoxOptions) options);
            case EDGE -> new EdgeDriver((EdgeOptions) options);
        };
    }

    private static Capabilities createOptions(
            BrowserType browserType, boolean headless, EnvironmentConfig config) {
        return switch (browserType) {
            case CHROME -> chromiumOptions(new ChromeOptions(), headless, config);
            case FIREFOX -> firefoxOptions(headless, config);
            case EDGE -> chromiumOptions(new EdgeOptions(), headless, config);
        };
    }

    private static WebDriver createRemoteDriver(Capabilities options, EnvironmentConfig config) {
        String gridUrl = config.getGridUrl();
        if (gridUrl == null || gridUrl.isBlank()) {
            throw new DriverInitializationException("gridUrl must be provided when remote=true");
        }
        try {
            return new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new DriverInitializationException("Invalid Selenium Grid URL: " + gridUrl, e);
        }
    }

    private static void configureDriver(
            WebDriver driver, boolean headless, EnvironmentConfig config) {
        driver.manage()
                .timeouts()
                .pageLoadTimeout(Duration.ofSeconds(config.getNavigationTimeout()));
        Dimension viewport = new Dimension(config.getWindowWidth(), config.getWindowHeight());
        if (headless) {
            driver.manage().window().setSize(viewport);
        } else {
            try {
                driver.manage().window().maximize();
            } catch (RuntimeException ignored) {
                driver.manage().window().setSize(viewport);
            }
        }
    }

    private static List<String> commonArguments(boolean headless, EnvironmentConfig config) {
        List<String> arguments = new ArrayList<>();
        if (headless) {
            arguments.add("--headless=new");
            arguments.add(
                    "--window-size=" + config.getWindowWidth() + "," + config.getWindowHeight());
            arguments.add("--force-device-scale-factor=1");
        }
        arguments.add("--no-sandbox");
        arguments.add("--disable-dev-shm-usage");
        return arguments;
    }

    private static <T extends ChromiumOptions<T>> T chromiumOptions(
            T options, boolean headless, EnvironmentConfig config) {
        options.addArguments(commonArguments(headless, config));
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless, EnvironmentConfig config) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments(
                    "-headless",
                    "--width=" + config.getWindowWidth(),
                    "--height=" + config.getWindowHeight());
        }
        return options;
    }
}
