package com.hoanhoang.report.listener;

import io.qameta.allure.Allure;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class TestRetryAnalyzer implements IRetryAnalyzer {
    private record RetryKey(long threadId, String method, String parameters) {}

    private final Map<RetryKey, Integer> attempts = new HashMap<>();

    private static RetryKey key(ITestResult result) {
        // TestNG executes a retry chain on its invocation's worker thread. Identical inputs on
        // different workers must not consume one another's retry budget.
        return new RetryKey(
                Thread.currentThread().threadId(),
                result.getMethod().getQualifiedName(),
                Arrays.deepToString(result.getParameters()));
    }

    static void resetAfterSuccess(ITestResult result) {
        if (result.getMethod().getRetryAnalyzer(result) instanceof TestRetryAnalyzer analyzer) {
            analyzer.reset(result);
        }
    }

    private synchronized void reset(ITestResult result) {
        attempts.remove(key(result));
    }

    @Override
    public synchronized boolean retry(ITestResult result) {
        int max = Integer.getInteger("retryCount", 0);
        if (max < 0) {
            throw new IllegalArgumentException("retryCount must not be negative");
        }
        RetryKey key = key(result);
        int count = attempts.getOrDefault(key, 0);
        if (count >= max) {
            attempts.remove(key);
            return false;
        }
        attempts.put(key, count + 1);
        result.setAttribute("retryAttempt", count + 1);
        Allure.addAttachment(
                "Retry requested",
                "Attempt "
                        + (count + 1)
                        + " of "
                        + max
                        + ". Previous failure is retained in this run; a retry is not a first-attempt pass.");
        return true;
    }
}
