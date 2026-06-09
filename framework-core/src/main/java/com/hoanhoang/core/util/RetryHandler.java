package com.hoanhoang.core.util;

public final class RetryHandler {

    private RetryHandler() {
    }

    public static void retry(Runnable action, int maxAttempts, int intervalMs) {
        retry(action, maxAttempts, intervalMs, null);
    }

    public static void retry(Runnable action, int maxAttempts, int intervalMs, Runnable onRetry) {
        Throwable lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                action.run();
                return;
            } catch (AssertionError | RuntimeException e) {
                lastError = e;
                if (attempt < maxAttempts) {
                    if (onRetry != null) {
                        onRetry.run();
                    }
                    sleep(intervalMs);
                }
            }
        }

        if (lastError instanceof AssertionError) {
            throw (AssertionError) lastError;
        }
        throw (RuntimeException) lastError;
    }

    private static void sleep(int intervalMs) {
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during retry", e);
        }
    }
}
