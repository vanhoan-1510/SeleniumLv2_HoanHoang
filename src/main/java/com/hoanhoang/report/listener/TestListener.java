package com.hoanhoang.report.listener;

import com.hoanhoang.report.allure.AllureManager;
import com.hoanhoang.report.logger.Log;
import io.qameta.allure.Allure;
import java.util.Arrays;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener, IConfigurationListener, IInvokedMethodListener {
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        // Capture while the Allure lifecycle and browser are still active, before onTestFailure.
        if (result.getStatus() == ITestResult.FAILURE) {
            attachFailure(result);
        }
    }

    private static void attachFailure(ITestResult result) {
        if (Boolean.TRUE.equals(result.getAttribute("failureContextAttached"))) {
            return;
        }
        if (Allure.getLifecycle().getCurrentTestCaseOrStep().isEmpty()) {
            return;
        }
        AllureManager.attachScreenshot("Failure: " + result.getName());
        AllureManager.attachFailureContext();
        result.setAttribute("failureContextAttached", true);
    }

    @Override
    public void onTestStart(ITestResult result) {
        Log.info("START " + name(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TestRetryAnalyzer.resetAfterSuccess(result);
        Log.info("PASS " + name(result) + duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Log.error("FAIL " + name(result) + duration(result) + " - " + reason(result));
        attachFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Log.warn("SKIP " + name(result) + " - " + reason(result));
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        Log.error("SETUP/CLEANUP FAIL " + result.getName() + " - " + reason(result));
        attachFailure(result);
    }

    /**
     * Class.method plus data-provider parameters, e.g.
     * TC03.userCanBuyAnItemWithEachPaymentMethod[COD].
     */
    private static String name(ITestResult result) {
        Object[] params = result.getParameters();
        return result.getTestClass().getRealClass().getSimpleName()
                + "."
                + result.getName()
                + (params == null || params.length == 0 ? "" : Arrays.toString(params));
    }

    private static String duration(ITestResult result) {
        return " (" + (result.getEndMillis() - result.getStartMillis()) / 1000.0 + "s)";
    }

    private static String reason(ITestResult result) {
        Throwable failure = result.getThrowable();
        if (failure == null) {
            return "no exception";
        }
        String message =
                failure.getMessage() == null
                        ? ""
                        : failure.getMessage().strip().lines().findFirst().orElse("");
        return failure.getClass().getSimpleName() + ": " + message;
    }
}
