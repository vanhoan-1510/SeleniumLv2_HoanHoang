package com.hoanhoang.report.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Log {

    private static final Logger LOGGER = LogManager.getLogger(Log.class);

    private Log() {}

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void step(String message) {
        LOGGER.info("[STEP] " + message);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }
}
