package com.hoanhoang.core.config;

public class EnvironmentConfig {
    private String baseUrl;
    private String browser;
    private boolean headless;
    private boolean remote;
    private String gridUrl;
    private int timeout;
    private int pollingInterval;
    private int elementTimeout;
    private int actionTimeout;
    private int assertionTimeout;
    private int navigationTimeout;
    private int stepRetryTimeoutMs;
    private int stepRetryIntervalMs;
    private int pageVerificationRetryTimeoutMs;
    private int testRetryTimeoutMs;
    private int testRetryIntervalMs;
    private int windowWidth;
    private int windowHeight;

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public boolean isHeadless() { return headless; }
    public boolean isRemote() { return remote; }
    public String getGridUrl() { return gridUrl; }
    public int getTimeout() { return timeout; }
    public int getPollingInterval() { return pollingInterval; }
    public int getElementTimeout() { return elementTimeout; }
    public int getActionTimeout() { return actionTimeout; }
    public int getAssertionTimeout() { return assertionTimeout; }
    public int getNavigationTimeout() { return navigationTimeout; }
    public int getStepRetryTimeoutMs() { return stepRetryTimeoutMs; }
    public int getStepRetryIntervalMs() { return stepRetryIntervalMs; }
    public int getPageVerificationRetryTimeoutMs() { return pageVerificationRetryTimeoutMs; }
    public int getTestRetryTimeoutMs() { return testRetryTimeoutMs; }
    public int getTestRetryIntervalMs() { return testRetryIntervalMs; }
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }

    // Setters
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setHeadless(boolean headless) { this.headless = headless; }
    public void setRemote(boolean remote) { this.remote = remote; }
    public void setGridUrl(String gridUrl) { this.gridUrl = gridUrl; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public void setPollingInterval(int pollingInterval) { this.pollingInterval = pollingInterval; }
    public void setElementTimeout(int elementTimeout) { this.elementTimeout = elementTimeout; }
    public void setActionTimeout(int actionTimeout) { this.actionTimeout = actionTimeout; }
    public void setAssertionTimeout(int assertionTimeout) { this.assertionTimeout = assertionTimeout; }
    public void setNavigationTimeout(int navigationTimeout) { this.navigationTimeout = navigationTimeout; }
    public void setStepRetryTimeoutMs(int stepRetryTimeoutMs) { this.stepRetryTimeoutMs = stepRetryTimeoutMs; }
    public void setStepRetryIntervalMs(int stepRetryIntervalMs) { this.stepRetryIntervalMs = stepRetryIntervalMs; }
    public void setPageVerificationRetryTimeoutMs(int pageVerificationRetryTimeoutMs) { this.pageVerificationRetryTimeoutMs = pageVerificationRetryTimeoutMs; }
    public void setTestRetryTimeoutMs(int testRetryTimeoutMs) { this.testRetryTimeoutMs = testRetryTimeoutMs; }
    public void setTestRetryIntervalMs(int testRetryIntervalMs) { this.testRetryIntervalMs = testRetryIntervalMs; }
    public void setWindowWidth(int windowWidth) { this.windowWidth = windowWidth; }
    public void setWindowHeight(int windowHeight) { this.windowHeight = windowHeight; }

    // Factory method to create from Properties
    public static EnvironmentConfig from(java.util.Properties props) {
        EnvironmentConfig config = new EnvironmentConfig();
        config.setBaseUrl(props.getProperty("baseUrl"));
        config.setBrowser(props.getProperty("browser"));
        config.setHeadless(Boolean.parseBoolean(props.getProperty("headless", "false")));
        config.setRemote(Boolean.parseBoolean(props.getProperty("remote", "false")));
        config.setGridUrl(props.getProperty("gridUrl", ""));
        config.setTimeout(Integer.parseInt(props.getProperty("timeout", "15")));
        config.setPollingInterval(Integer.parseInt(props.getProperty("pollingInterval", "500")));
        config.setElementTimeout(Integer.parseInt(props.getProperty("elementTimeout", "10")));
        config.setActionTimeout(Integer.parseInt(props.getProperty("actionTimeout", "8")));
        config.setAssertionTimeout(Integer.parseInt(props.getProperty("assertionTimeout", "5")));
        config.setNavigationTimeout(Integer.parseInt(props.getProperty("navigationTimeout", "20")));
        int legacyRetryIntervalMs = parseInt(props, "retryIntervalMs", 500);
        int legacyRetryTimeoutMs = parseInt(props, "maxRetryAttempts", 2) * legacyRetryIntervalMs;
        config.setStepRetryTimeoutMs(parseInt(props, "stepRetryTimeoutMs", legacyRetryTimeoutMs));
        config.setStepRetryIntervalMs(parseInt(props, "stepRetryIntervalMs", legacyRetryIntervalMs));
        config.setPageVerificationRetryTimeoutMs(parseInt(props, "pageVerificationRetryTimeoutMs", config.getStepRetryTimeoutMs()));
        config.setTestRetryTimeoutMs(parseInt(props, "testRetryTimeoutMs", 0));
        config.setTestRetryIntervalMs(parseInt(props, "testRetryIntervalMs", config.getStepRetryIntervalMs()));
        config.setWindowWidth(parseInt(props, "windowWidth", 1920));
        config.setWindowHeight(parseInt(props, "windowHeight", 1080));
        return config;
    }

    private static int parseInt(java.util.Properties props, String key, int defaultValue) {
        return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
    }
}
