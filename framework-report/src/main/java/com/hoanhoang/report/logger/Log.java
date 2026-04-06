package com.hoanhoang.report.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

    private static final Logger logger = LogManager.getLogger(Log.class);

    public static void step(String message) {
        logger.info("[STEP] " + message);
    }

    public static void info(String message) {
        logger.info("[INFO] " + message);
    }

    public static void debug(String message) {
        logger.debug("[DEBUG] " + message);
    }

    public static void error(String message) {
        logger.error("[ERROR] " + message);
    }

    public static void warning(String message) {
        logger.warn("[WARNING] " + message);
    }
}