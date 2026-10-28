package com.hoanhoang.unit.support;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** TestNG fixtures launched by FrameworkTest in a separate JVM; no browser or network. */
public final class RetryLifecycleProbe {
    private RetryLifecycleProbe() {}

    public static class Repeated {
        private final AtomicInteger calls = new AtomicInteger();

        @Test(invocationCount = 2)
        public void retriesEachInvocation() {
            if (calls.incrementAndGet() % 2 == 1) {
                throw new AssertionError("Transient fixture failure");
            }
        }
    }

    public static class DuplicateData {
        private final AtomicInteger calls = new AtomicInteger();

        @DataProvider(name = "duplicates")
        public Object[][] duplicates() {
            return new Object[][] {{"same"}, {"same"}};
        }

        @Test(dataProvider = "duplicates")
        public void retriesEachDataRow(String value) {
            if (calls.incrementAndGet() % 2 == 1) {
                throw new AssertionError("Transient fixture failure: " + value);
            }
        }
    }

    public static class Parallel {
        private final ThreadLocal<Integer> calls = ThreadLocal.withInitial(() -> 0);
        private final CyclicBarrier failures = new CyclicBarrier(2);
        private final CyclicBarrier successes = new CyclicBarrier(2);

        @Test(invocationCount = 4, threadPoolSize = 2)
        public void retriesEachWorkerIndependently() throws Exception {
            int call = calls.get() + 1;
            calls.set(call);
            if (call % 2 == 1) {
                failures.await(5, TimeUnit.SECONDS);
                throw new AssertionError("Transient fixture failure on worker");
            }
            successes.await(5, TimeUnit.SECONDS);
        }
    }

    public static class AlwaysFails {
        static final AtomicInteger calls = new AtomicInteger();

        @Test
        public void exhaustsBudget() {
            calls.incrementAndGet();
            throw new AssertionError("Persistent fixture failure");
        }
    }

    private static TestNG run(Class<?>... fixtures) {
        TestNG runner = new TestNG();
        runner.setUseDefaultListeners(false);
        runner.setTestClasses(fixtures);
        runner.run();
        return runner;
    }

    public static void main(String[] args) {
        if (run(Repeated.class, DuplicateData.class, Parallel.class).hasFailure()) {
            throw new AssertionError("A fresh invocation lost its retry budget");
        }

        if (!run(AlwaysFails.class).hasFailure() || AlwaysFails.calls.get() != 2) {
            throw new AssertionError("retryCount=1 must stop after two attempts");
        }

        AlwaysFails.calls.set(0);
        System.setProperty("retryCount", "0");
        if (!run(AlwaysFails.class).hasFailure() || AlwaysFails.calls.get() != 1) {
            throw new AssertionError("retryCount=0 must not retry");
        }
        System.out.println("Retry lifecycle regression checks passed");
    }
}
