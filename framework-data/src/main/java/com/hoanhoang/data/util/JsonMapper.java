package com.hoanhoang.data.util;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

public class JsonMapper {

    private static final Gson gson = new Gson();

    public static <T> T fromFile(String filePath, Class<T> classType) {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, classType);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file: " + filePath, e);
        }
    }

    public static <T> T fromJson(String json, Class<T> classType) {
        return gson.fromJson(json, classType);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}