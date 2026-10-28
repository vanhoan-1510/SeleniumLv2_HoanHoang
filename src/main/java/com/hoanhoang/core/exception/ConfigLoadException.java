package com.hoanhoang.core.exception;

public class ConfigLoadException extends RuntimeException {
    public ConfigLoadException(String env, String reason, Throwable cause) {
        super(
                String.format("Failed to load configuration for environment '%s': %s", env, reason),
                cause);
    }
}
