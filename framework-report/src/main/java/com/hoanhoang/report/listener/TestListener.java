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
        print("START", result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Log.info("Test passed: " + result.getName());
        print("PASS", result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Log.error("Test failed: " + result.getName());
        print("FAIL", result);
        if (DriverManager.getDriver() != null) {
            AllureManager.attachScreenshot("Screenshot on failure");
        } else {
            Log.warn("Driver was null at test failure - screenshot not available");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Log.warn("Test skipped: " + result.getName());
        print("SKIP", result);
    }

    private void print(String status, ITestResult result) {
        long durationMs = result.getEndMillis() > result.getStartMillis()
                ? result.getEndMillis() - result.getStartMillis()
                : 0;
        System.out.printf(
                "[%s] [%s] %s.%s (%d ms)%n",
                status,
                Thread.currentThread().getName(),
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                durationMs
        );
    }
}
