package com.hoanhoang.core.config;

import com.hoanhoang.core.driver.BrowserType;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

/** Loaded once from JSON; callers receive read-only access through getters. */
public final class EnvironmentConfig {
    private String baseUrl;
    private String browser = "chrome";
    private boolean headless;
    private boolean remote;
    private String gridUrl = "";
    private int pollingInterval = 200;
    private int elementTimeout = 10;
    private int actionTimeout = 10;
    private int navigationTimeout = 30;
    private int uiTimeoutMs = 15000;
    private int windowWidth = 1920;
    private int windowHeight = 1080;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getGridUrl() {
        return gridUrl;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public int getElementTimeout() {
        return elementTimeout;
    }

    public int getActionTimeout() {
        return actionTimeout;
    }

    public int getNavigationTimeout() {
        return navigationTimeout;
    }

    public int getUiTimeoutMs() {
        return uiTimeoutMs;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    /** Returns a separate snapshot; published configurations are never mutated. */
    public EnvironmentConfig withOverrides(Properties properties) {
        EnvironmentConfig result = new EnvironmentConfig();
        result.baseUrl = properties.getProperty("baseUrl", baseUrl);
        result.browser = properties.getProperty("browser", browser);
        result.headless = booleanProperty(properties, "headless", headless);
        result.remote = booleanProperty(properties, "remote", remote);
        result.gridUrl = properties.getProperty("gridUrl", gridUrl);
        result.pollingInterval = intProperty(properties, "pollingInterval", pollingInterval);
        result.elementTimeout = intProperty(properties, "elementTimeout", elementTimeout);
        result.actionTimeout = intProperty(properties, "actionTimeout", actionTimeout);
        result.navigationTimeout = intProperty(properties, "navigationTimeout", navigationTimeout);
        result.uiTimeoutMs = intProperty(properties, "uiTimeoutMs", uiTimeoutMs);
        result.windowWidth = intProperty(properties, "windowWidth", windowWidth);
        result.windowHeight = intProperty(properties, "windowHeight", windowHeight);
        return result;
    }

    private static boolean booleanProperty(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(value.trim())) {
            return false;
        }
        throw new IllegalArgumentException(key + " must be true or false");
    }

    private static int intProperty(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be an integer", e);
        }
    }

    public void validate(String environment) {
        validateUrl(baseUrl, "baseUrl");
        validateBrowser(environment);
        if (remote) {
            validateUrl(gridUrl, "gridUrl");
        }
        validatePositiveValues(environment);
    }

    private void validateBrowser(String environment) {
        try {
            BrowserType.valueOf(browser.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Unsupported browser in " + environment + ": " + browser, e);
        }
    }

    private void validatePositiveValues(String environment) {
        if (pollingInterval <= 0
                || elementTimeout <= 0
                || actionTimeout <= 0
                || navigationTimeout <= 0
                || uiTimeoutMs <= 0
                || windowWidth <= 0
                || windowHeight <= 0) {
            throw new IllegalArgumentException(
                    "Timeouts, polling interval and viewport must be positive in " + environment);
        }
    }

    private static void validateUrl(String value, String key) {
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        URI uri = URI.create(value);
        if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalArgumentException(key + " must be an absolute HTTP(S) URL");
        }
    }
}
