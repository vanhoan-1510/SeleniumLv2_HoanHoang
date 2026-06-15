package com.hoanhoang.core.util;

public final class RetryHandler {

    private RetryHandler() {
    }

    public static void retryUntilTimeout(Runnable action, long timeoutMs, long intervalMs) {
        retryUntilTimeout(action, timeoutMs, intervalMs, null);
    }

    public static void retryUntilTimeout(Runnable action, long timeoutMs, long intervalMs, Runnable onRetry) {
        long safeTimeoutMs = Math.max(timeoutMs, 0);
        long deadline = System.currentTimeMillis() + safeTimeoutMs;
        Throwable lastError = null;

        while (true) {
            try {
                action.run();
                return;
            } catch (AssertionError | RuntimeException e) {
                lastError = e;
                if (safeTimeoutMs == 0 || System.currentTimeMillis() >= deadline) {
                    break;
                }

                if (onRetry != null) {
                    onRetry.run();
                }

                long remainingMs = deadline - System.currentTimeMillis();
                if (remainingMs <= 0) {
                    break;
                }
                sleep(Math.min(Math.max(intervalMs, 0), remainingMs));
            }
        }

        throwLastError(lastError);
    }

    private static void throwLastError(Throwable lastError) {
        if (lastError instanceof AssertionError) {
            throw (AssertionError) lastError;
        }
        throw (RuntimeException) lastError;
    }

    private static void sleep(long intervalMs) {
        if (intervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during retry", e);
        }
    }
}
