package com.hoanhoang.core.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hoanhoang.core.constant.FrameworkConstants;
import com.hoanhoang.core.exception.ConfigLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads framework configuration from JSON files.
 * Supports dev, staging, and production environments.
 */
public class JsonConfigLoader {

    private static final Gson gson = new Gson();

    /**
     * Loads configuration from JSON file for specified environment.
     *
     * @param env environment name (e.g., "dev", "staging", "prod")
     * @return EnvironmentConfig loaded from JSON
     * @throws ConfigLoadException if file not found or parsing fails
     */
    public static EnvironmentConfig loadFromJson(String env) {
        String resourcePath = FrameworkConstants.CONFIG_DIR + env + ".json";

        try (InputStream is = JsonConfigLoader.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new ConfigLoadException(
                    env,
                    "Config file not found in classpath: " + resourcePath + "\n" +
                    "Please create this file in src/test/resources/config/ folder",
                    null
                );
            }

            JsonObject json = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);

            // Validate required fields
            if (!json.has("baseUrl")) {
                throw new ConfigLoadException(env, "baseUrl is required in configuration", null);
            }

            if (!json.has("browser")) {
                throw new ConfigLoadException(env, "browser is required in configuration", null);
            }

            // Convert JSON properties to Java Properties-like format
            java.util.Properties props = new java.util.Properties();
            json.keySet().forEach(key -> {
                if (json.get(key) != null && !json.get(key).isJsonNull()) {
                    Object value = json.get(key);
                    if (value instanceof com.google.gson.JsonPrimitive) {
                        props.setProperty(key, ((JsonPrimitive) value).getAsString());
                    }
                }
            });

            return EnvironmentConfig.from(props);

        } catch (IOException e) {
            throw new ConfigLoadException(
                env,
                "Failed to load config file: " + resourcePath + ". " + e.getMessage(),
                e
            );
        }
    }

    private JsonConfigLoader() {
        // Prevent instantiation
    }
}