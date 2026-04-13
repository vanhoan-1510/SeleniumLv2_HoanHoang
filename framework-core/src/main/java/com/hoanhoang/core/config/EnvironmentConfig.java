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
}