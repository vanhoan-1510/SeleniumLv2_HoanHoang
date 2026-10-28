package com.hoanhoang.core.config;

import com.hoanhoang.core.constant.FrameworkConstants;
import com.hoanhoang.core.exception.ConfigLoadException;
import com.hoanhoang.core.util.JsonMapper;

public final class JsonConfigLoader {
    private JsonConfigLoader() {}

    public static EnvironmentConfig loadFromJson(String environment) {
        EnvironmentConfig config = readFromJson(environment);
        config.validate(environment);
        return config;
    }

    static EnvironmentConfig readFromJson(String environment) {
        if (environment == null || !environment.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid environment name");
        }
        String path = FrameworkConstants.CONFIG_DIR + environment + ".json";
        try {
            return JsonMapper.readResource(path, EnvironmentConfig.class);
        } catch (IllegalStateException e) {
            throw new ConfigLoadException(
                    environment, "Invalid configuration resource: " + path, e);
        }
    }
}
