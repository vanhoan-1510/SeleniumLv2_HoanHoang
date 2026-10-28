package com.hoanhoang.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON <-> object mapping with Gson. Resources are read from the classpath (src/test/resources).
 */
public final class JsonMapper {
    private static final Gson GSON = new Gson();

    private JsonMapper() {}

    public static <T> T readResource(String path, Class<T> type) {
        try (InputStream input = JsonMapper.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            T value = GSON.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), type);
            if (value == null) {
                throw new IllegalStateException("Empty JSON resource: " + path);
            }
            return value;
        } catch (IOException | JsonParseException e) {
            throw new IllegalStateException("Cannot read JSON resource: " + path, e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T readFile(Path path, Class<T> type) {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            T value = GSON.fromJson(reader, type);
            if (value == null) {
                throw new IllegalStateException("Empty JSON file: " + path);
            }
            return value;
        } catch (IOException | JsonParseException e) {
            // Do not expose JSON parser messages that can contain credential values.
            throw new IllegalStateException("Cannot read JSON file: " + path);
        }
    }

    public static <T> T convert(JsonElement json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    public static String toJson(Object value) {
        return GSON.toJson(value);
    }
}
