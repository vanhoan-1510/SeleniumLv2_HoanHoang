package com.hoanhoang.core.config;

public class EnvironmentConfig {
    private String baseUrl;
    private String browser;
    private boolean headless;
    private int timeout;

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public boolean isHeadless() { return headless; }
    public int getTimeout() { return timeout; }

    // Setters
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setHeadless(boolean headless) { this.headless = headless; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    // Factory method to create from Properties
    public static EnvironmentConfig from(java.util.Properties props) {
        EnvironmentConfig config = new EnvironmentConfig();
        config.setBaseUrl(props.getProperty("baseUrl"));
        config.setBrowser(props.getProperty("browser"));
        config.setHeadless(Boolean.parseBoolean(props.getProperty("headless", "false")));
        config.setTimeout(Integer.parseInt(props.getProperty("timeout", "30")));
        return config;
    }
}