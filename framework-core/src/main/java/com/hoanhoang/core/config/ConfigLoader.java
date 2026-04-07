package com.hoanhoang.core.config;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigLoader {

    private static EnvironmentConfig config;

    public static EnvironmentConfig getConfig() {
        return config;
    }


    public static void load(String env) {
        // The path inside the 'resources' folder
        String resourcePath = "config/" + env + ".json";

        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Targeted config file not found on classpath: " + resourcePath);
            }

            // Use InputStreamReader to handle the stream with UTF-8 encoding
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                config = new Gson().fromJson(reader, EnvironmentConfig.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Critical failure: Failed to load or parse config for env: " + env, e);
        }
    }
}