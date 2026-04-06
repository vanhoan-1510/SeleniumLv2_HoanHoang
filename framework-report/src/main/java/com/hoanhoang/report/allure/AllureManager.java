package com.hoanhoang.report.allure;

import com.hoanhoang.core.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

public class AllureManager {

    public static void attachScreenshot(String name) {
        byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(name, new ByteArrayInputStream(screenshot));
    }

    public static void attachText(String name, String content) {
        Allure.addAttachment(name, content);
    }
}