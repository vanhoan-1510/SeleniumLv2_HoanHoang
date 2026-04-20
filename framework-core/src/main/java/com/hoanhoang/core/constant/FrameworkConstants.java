package com.hoanhoang.core.constant;

/**
 * Framework-wide constants for paths and resource directories.
 * All runtime configuration values (timeouts, browser settings, etc.) are read from JSON files.
 * Centralizes directory paths to improve maintainability.
 */
public class FrameworkConstants {

    // Resource directory paths
    /**
     * Directory path for test data JSON files.
     */
    public static final String TEST_DATA_DIR = "data/";

    /**
     * Directory path for configuration files.
     */
    public static final String CONFIG_DIR = "config/";

    /**
     * Directory path for log files.
     */
    public static final String LOG_DIR = "logs/";

    private FrameworkConstants() {
        // Prevent instantiation
    }
}
