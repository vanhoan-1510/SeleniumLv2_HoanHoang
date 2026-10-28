package com.hoanhoang.pages.support;

import com.hoanhoang.core.util.UiWait;
import org.openqa.selenium.JavascriptExecutor;

/** Readiness rules belonging to this WooCommerce application. */
public final class ApplicationWait {
    private ApplicationWait() {}

    public static void idle() {
        UiWait.until(
                "Page document and WooCommerce updates complete",
                driver -> {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    if (Boolean.TRUE.equals(
                            js.executeScript(
                                    "return !!document.body && document.body.innerText.includes('There has been a critical error on this website');"))) {
                        throw new IllegalStateException(
                                "Site returned WordPress critical error: "
                                        + driver.getCurrentUrl());
                    }
                    return Boolean.TRUE.equals(
                            js.executeScript(
                                    "return document.readyState === 'complete' && "
                                            + "(!window.jQuery || jQuery.active === 0) && "
                                            + "!Array.from(document.querySelectorAll('.blockUI.blockOverlay, .et-loader.loading')).some(e => "
                                            + "e.getClientRects().length && getComputedStyle(e).visibility !== 'hidden');"));
                });
    }
}
