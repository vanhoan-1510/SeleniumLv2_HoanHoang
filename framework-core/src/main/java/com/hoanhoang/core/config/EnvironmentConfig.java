package com.hoanhoang.core.config;

public class EnvironmentConfig {
    private String baseUrl;
    private String browser;
    private boolean headless;
    private int timeout;
    private int pollingInterval;
    private int elementTimeout;
    private int actionTimeout;
    private int assertionTimeout;
    private int navigationTimeout;
    private int maxRetryAttempts;
    private int retryIntervalMs;

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public boolean isHeadless() { return headless; }
    public int getTimeout() { return timeout; }
    public int getPollingInterval() { return pollingInterval; }
    public int getElementTimeout() { return elementTimeout; }
    public int getActionTimeout() { return actionTimeout; }
    public int getAssertionTimeout() { return assertionTimeout; }
    public int getNavigationTimeout() { return navigationTimeout; }
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public int getRetryIntervalMs() { return retryIntervalMs; }

    // Setters
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setHeadless(boolean headless) { this.headless = headless; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public void setPollingInterval(int pollingInterval) { this.pollingInterval = pollingInterval; }
    public void setElementTimeout(int elementTimeout) { this.elementTimeout = elementTimeout; }
    public void setActionTimeout(int actionTimeout) { this.actionTimeout = actionTimeout; }
    public void setAssertionTimeout(int assertionTimeout) { this.assertionTimeout = assertionTimeout; }
    public void setNavigationTimeout(int navigationTimeout) { this.navigationTimeout = navigationTimeout; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    public void setRetryIntervalMs(int retryIntervalMs) { this.retryIntervalMs = retryIntervalMs; }

    // Factory method to create from Properties
    public static EnvironmentConfig from(java.util.Properties props) {
        EnvironmentConfig config = new EnvironmentConfig();
        config.setBaseUrl(props.getProperty("baseUrl"));
        config.setBrowser(props.getProperty("browser"));
        config.setHeadless(Boolean.parseBoolean(props.getProperty("headless", "false")));
        config.setTimeout(Integer.parseInt(props.getProperty("timeout", "15")));
        config.setPollingInterval(Integer.parseInt(props.getProperty("pollingInterval", "500")));
        config.setElementTimeout(Integer.parseInt(props.getProperty("elementTimeout", "10")));
        config.setActionTimeout(Integer.parseInt(props.getProperty("actionTimeout", "8")));
        config.setAssertionTimeout(Integer.parseInt(props.getProperty("assertionTimeout", "5")));
        config.setNavigationTimeout(Integer.parseInt(props.getProperty("navigationTimeout", "20")));
        config.setMaxRetryAttempts(Integer.parseInt(props.getProperty("maxRetryAttempts", "2")));
        config.setRetryIntervalMs(Integer.parseInt(props.getProperty("retryIntervalMs", "500")));
        return config;
    }
}