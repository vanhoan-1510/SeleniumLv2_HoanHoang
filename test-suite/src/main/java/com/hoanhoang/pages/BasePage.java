package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.core.element.ElementsFactory;
import com.hoanhoang.core.util.RetryHandler;
import org.openqa.selenium.By;

public class BasePage {

    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }

    protected Elements $$(By locator) {
        return ElementsFactory.$$(locator);
    }

    protected Element find(By locator) {
        return ElementFactory.$(locator);
    }

    protected void verifyWithRetry(Runnable verification) {
        RetryHandler.retryUntilTimeout(
                verification,
                ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(),
                ConfigLoader.getConfig().getStepRetryIntervalMs(),
                this::retryPageLoad
        );
    }

    protected void retryPageLoad() {
        DriverManager.getDriver().navigate().refresh();
    }

    protected void assertEquals(String message, String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ". Expected: '" + expected + "', Actual: '" + actual + "'");
        }
    }

    protected void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    protected void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}
