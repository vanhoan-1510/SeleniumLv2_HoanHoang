package com.hoanhoang.core.config;

public class ConfigLoader {

    private static EnvironmentConfig config;

    public static EnvironmentConfig getConfig() {
        return config;
    }

    public static void load(String env) {
        config = JsonConfigLoader.loadFromJson(env);
    }
}