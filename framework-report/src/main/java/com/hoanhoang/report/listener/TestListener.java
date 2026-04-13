package com.hoanhoang.report.listener;

import com.hoanhoang.report.allure.AllureManager;
import com.hoanhoang.report.logger.Log;
import com.hoanhoang.core.driver.DriverManager;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        Log.step("Starting test: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Log.info("Test passed: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Log.error("Test failed: " + result.getName());
        if (DriverManager.getDriver() != null) {
            AllureManager.attachScreenshot("Screenshot on failure");
        } else {
            Log.warn("Driver was null at test failure - screenshot not available");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Log.warn("Test skipped: " + result.getName());
    }
}