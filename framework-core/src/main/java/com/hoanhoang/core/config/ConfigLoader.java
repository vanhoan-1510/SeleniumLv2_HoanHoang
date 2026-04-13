package com.hoanhoang.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigLoader {

    private static EnvironmentConfig config;

    public static EnvironmentConfig getConfig() {
        return config;
    }

    public static void load(String env) {
        // The path inside the 'resources' folder
        String resourcePath = "config/" + env + ".properties";

        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException(
                    "Config file not found: " + resourcePath + "\n" +
                    "Please create this file in src/test/resources/config/ folder or use -Denv=<name> to specify a valid environment.\n" +
                    "Available environments: dev, staging (or your custom environment)"
                );
            }

            // Use Properties to load the configuration
            Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            config = new EnvironmentConfig();
            config.setBaseUrl(properties.getProperty("baseUrl"));
            config.setBrowser(properties.getProperty("browser"));
            config.setHeadless(Boolean.parseBoolean(properties.getProperty("headless")));
            config.setTimeout(Integer.parseInt(properties.getProperty("timeout")));

        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to load config for environment: " + env + "\n" +
                "Error: " + e.getMessage() + "\n" +
                "Please check your properties file syntax and required fields: baseUrl, browser, headless, timeout\n" +
                "See sample.properties for a valid configuration example.",
                e
            );
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                "Failed to parse timeout value for environment: " + env + "\n" +
                "Error: " + e.getMessage() + "\n" +
                "The 'timeout' field must be a valid integer.",
                e
            );
        }
    }
}