package com.hoanhoang.core.util;

import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.JavascriptExecutor;

/** Executes browser scripts; element-specific scripts belong to the element wrapper. */
public final class JavaScript {
    private JavaScript() {}

    public static Object execute(String script, Object... arguments) {
        return ((JavascriptExecutor) DriverManager.getDriver()).executeScript(script, arguments);
    }
}
