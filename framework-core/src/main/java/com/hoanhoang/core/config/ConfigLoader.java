package com.hoanhoang.core.config;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {

    private static EnvironmentConfig config;

    public static EnvironmentConfig getConfig() {
        return config;
    }

    public static void load(String env) {
        String filePath = "src/test/resources/config/" + env + ".json";
        try (FileReader reader = new FileReader(filePath)) {
            config = new Gson().fromJson(reader, EnvironmentConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config for env: " + env, e);
        }
    }
}