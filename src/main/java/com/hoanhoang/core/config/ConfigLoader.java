package com.hoanhoang.core.config;

import java.util.Locale;

public final class ConfigLoader {

    private static final Object LOAD_LOCK = new Object();
    private static volatile EnvironmentConfig config;
    private static volatile String loadedEnvironment;

    private ConfigLoader() {}

    public static EnvironmentConfig getConfig() {
        EnvironmentConfig currentConfig = config;
        if (currentConfig == null) {
            throw new IllegalStateException(
                    "Configuration has not been loaded. Call ConfigLoader.load(env) before using the framework.");
        }
        return currentConfig;
    }

    public static void load(String environment) {
        String normalizedEnvironment = normalizeEnvironment(environment);
        if (normalizedEnvironment.equals(loadedEnvironment) && config != null) {
            return;
        }

        synchronized (LOAD_LOCK) {
            if (normalizedEnvironment.equals(loadedEnvironment) && config != null) {
                return;
            }
            if (loadedEnvironment != null && !loadedEnvironment.equals(normalizedEnvironment)) {
                throw new IllegalStateException(
                        "Configuration is already loaded for environment '"
                                + loadedEnvironment
                                + "' and cannot be changed to '"
                                + normalizedEnvironment
                                + "' during the same run");
            }

            EnvironmentConfig loadedConfig =
                    JsonConfigLoader.readFromJson(normalizedEnvironment)
                            .withOverrides(System.getProperties());
            loadedConfig.validate(normalizedEnvironment);
            config = loadedConfig;
            loadedEnvironment = normalizedEnvironment;
        }
    }

    private static String normalizeEnvironment(String environment) {
        if (environment == null || environment.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment name must not be blank");
        }
        return environment.trim().toLowerCase(Locale.ROOT);
    }
}
