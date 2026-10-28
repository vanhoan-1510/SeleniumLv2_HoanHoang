package com.hoanhoang.report.allure;

import com.hoanhoang.core.driver.DriverManager;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

public final class AllureManager {
    private AllureManager() {}

    public static void attachScreenshot(String name) {
        if (Allure.getLifecycle().getCurrentTestCaseOrStep().isEmpty()) {
            return;
        }
        if (!DriverManager.hasDriver()) {
            attachText(name, "Browser unavailable");
            return;
        }
        try {
            if (!(DriverManager.getDriver() instanceof TakesScreenshot screenshot)) {
                attachText(name, "Driver does not support screenshots");
                return;
            }
            byte[] bytes = screenshot.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png");
        } catch (WebDriverException e) {
            attachText(name, "Screenshot unavailable: " + e.getClass().getSimpleName());
        }
    }

    public static void attachText(String name, String content) {
        Allure.addAttachment(name, content);
    }

    public static void attachFailureContext() {
        if (Allure.getLifecycle().getCurrentTestCaseOrStep().isEmpty()
                || !DriverManager.hasDriver()) {
            return;
        }
        try {
            WebDriver driver = DriverManager.getDriver();
            attachText(
                    "Browser location",
                    "URL: " + driver.getCurrentUrl() + "\nTitle: " + driver.getTitle());
            String source = driver.getPageSource();
            if (source != null) {
                Allure.addAttachment(
                        "Page source at failure",
                        "text/html",
                        new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)),
                        ".html");
            }
        } catch (WebDriverException e) {
            attachText("Browser context unavailable", e.getClass().getSimpleName());
        }
    }
}
