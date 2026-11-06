package com.hoanhoang.pages.support;

import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.JavaScript;
import com.hoanhoang.core.util.UiWait;
import io.qameta.allure.Allure;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

/** Readiness rules belonging to this WooCommerce application. */
public final class ApplicationWait {
    private static final String CRITICAL_ERROR_SCRIPT =
            "return !!document.body && document.body.innerText.includes('There has been a critical error on this website');";
    private static final String READINESS_STATE_SCRIPT =
            """
            return JSON.stringify({
              readyState: document.readyState,
              jqueryActive: window.jQuery ? jQuery.active : null,
              visibleBlockers: Array.from(document.querySelectorAll(
                '.blockUI.blockOverlay, .et-loader.loading'
              )).filter(e => e.getClientRects().length &&
                getComputedStyle(e).visibility !== 'hidden'
              ).map(e => e.className),
              checkoutRequests: performance.getEntriesByType('resource')
                .filter(e => e.name.includes('wc-ajax=update_order_review')).slice(-5)
                .map(e => ({endpoint: 'update_order_review', status: e.responseStatus ?? null,
                  durationMs: Math.round(e.duration)}))
            });
            """;

    private ApplicationWait() {}

    public static void failIfCriticalError() {
        if (Boolean.TRUE.equals(JavaScript.execute(CRITICAL_ERROR_SCRIPT))) {
            throw new IllegalStateException(
                    "Site returned WordPress critical error: "
                            + DriverManager.getDriver().getCurrentUrl());
        }
    }

    public static void idle() {
        try {
            waitUntilIdle();
        } catch (TimeoutException timeout) {
            String state;
            try {
                state = String.valueOf(JavaScript.execute(READINESS_STATE_SCRIPT));
            } catch (WebDriverException diagnosticsFailure) {
                timeout.addSuppressed(diagnosticsFailure);
                throw timeout;
            }
            Allure.addAttachment("Page readiness at timeout", state);
            throw new TimeoutException("Page readiness timed out; last state: " + state, timeout);
        }
    }

    private static void waitUntilIdle() {
        UiWait.until(
                "Page document and WooCommerce updates complete",
                driver -> {
                    failIfCriticalError();
                    return Boolean.TRUE.equals(
                            JavaScript.execute(
                                    "return document.readyState === 'complete' && "
                                            + "(!window.jQuery || jQuery.active === 0) && "
                                            + "!Array.from(document.querySelectorAll('.blockUI.blockOverlay, .et-loader.loading')).some(e => "
                                            + "e.getClientRects().length && getComputedStyle(e).visibility !== 'hidden');"));
                });
    }
}
