package com.hoanhoang.report.logger;

import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;

/**
 * Mirrors every Allure @Step into the log, so report steps and log steps never drift apart.
 * Registered in META-INF/services/io.qameta.allure.listener.StepLifecycleListener.
 */
public class AllureStepLogger implements StepLifecycleListener {
    /**
     * Allure.step(name, ...) starts with the placeholder name "step" and renames it right after.
     */
    private static final String PLACEHOLDER = "step";

    @Override
    public void beforeStepStart(StepResult result) {
        if (!PLACEHOLDER.equals(result.getName())) {
            Log.step(result.getName());
        }
    }

    @Override
    public void afterStepUpdate(StepResult result) {
        // Rename of a programmatic step (status still unset): log it under its real name.
        if (result.getStatus() == null && !PLACEHOLDER.equals(result.getName())) {
            Log.step(result.getName());
        }
    }

    @Override
    public void afterStepStop(StepResult result) {
        if (result.getStatus() == Status.FAILED || result.getStatus() == Status.BROKEN) {
            String reason =
                    result.getStatusDetails() == null ? "" : result.getStatusDetails().getMessage();
            Log.warn(
                    "Step "
                            + result.getStatus()
                            + ": "
                            + result.getName()
                            + (reason == null ? "" : " - " + reason));
        }
    }
}
