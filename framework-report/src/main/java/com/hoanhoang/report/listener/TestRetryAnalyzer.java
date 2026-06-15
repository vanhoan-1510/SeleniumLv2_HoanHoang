package com.hoanhoang.report.listener;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import com.hoanhoang.report.logger.Log;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class TestRetryAnalyzer implements IRetryAnalyzer {

    private long retryStartedAtMs;

    @Override
    public boolean retry(ITestResult result) {
        EnvironmentConfig config = ConfigLoader.getConfig();
        if (config == null || config.getTestRetryTimeoutMs() <= 0) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (retryStartedAtMs == 0) {
            retryStartedAtMs = now;
        }

        long elapsedMs = now - retryStartedAtMs;
        if (elapsedMs >= config.getTestRetryTimeoutMs()) {
            Log.warn("Test retry timeout reached for: " + result.getName());
            return false;
        }

        sleep(config.getTestRetryIntervalMs());
        Log.warn("Retrying failed test: " + result.getName());
        return true;
    }

    private void sleep(long intervalMs) {
        if (intervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted before retrying test", e);
        }
    }
}
