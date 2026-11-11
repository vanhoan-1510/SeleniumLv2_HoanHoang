package com.hoanhoang.core.driver;

import org.openqa.selenium.JavascriptExecutor;

/** Browser operations using the driver registered for the current thread. */
public final class DriverUtils {
    private DriverUtils() {}

    public static void open(String url) {
        DriverManager.getDriver().get(url);
    }

    public static void refresh() {
        DriverManager.getDriver().navigate().refresh();
    }

    public static Object executeScript(String script, Object... arguments) {
        return ((JavascriptExecutor) DriverManager.getDriver()).executeScript(script, arguments);
    }
}
