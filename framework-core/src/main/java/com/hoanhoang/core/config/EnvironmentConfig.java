package com.hoanhoang.core.config;

public class EnvironmentConfig {
    private String baseUrl;
    private String browser;
    private boolean headless;
    private int timeout;
    private boolean remote;
    private String gridUrl;

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public boolean isHeadless() { return headless; }
    public int getTimeout() { return timeout; }
    public boolean isRemote() { return remote; }
    public String getGridUrl() { return gridUrl; }
}