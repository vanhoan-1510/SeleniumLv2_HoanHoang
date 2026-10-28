package com.hoanhoang.tests.support;

import com.hoanhoang.core.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;

/** Read-only AJAX observations for diagnosing the demo site's quantity updates. */
public final class CartUpdateTrace implements AutoCloseable {
    public CartUpdateTrace() {
        ((JavascriptExecutor) DriverManager.getDriver())
                .executeScript(
                        """
                window.frameworkCartTrace = [];
                jQuery(document).on('ajaxSend.frameworkCartTrace', function(e,x,o) {
                    const quantities = {};
                    new URLSearchParams(typeof o.data === 'string' ? o.data : '').forEach((v,k) => {
                        if (/^cart\\[.*\\]\\[qty\\]$/.test(k)) quantities[k] = v;
                    });
                    window.frameworkCartTrace.push({event:'send',path:new URL(o.url,location.href).pathname,
                        quantities:quantities,time:Date.now()});
                }).on('ajaxComplete.frameworkCartTrace', function(e,x,o) {
                    window.frameworkCartTrace.push({event:'complete',path:new URL(o.url,location.href).pathname,
                        status:x.status,critical:(x.responseText||'').includes('There has been a critical error'),
                        time:Date.now()});
                });
                """);
    }

    @Override
    public void close() {
        try {
            String trace =
                    String.valueOf(
                            ((JavascriptExecutor) DriverManager.getDriver())
                                    .executeScript(
                                            "jQuery(document).off('.frameworkCartTrace'); return JSON.stringify(window.frameworkCartTrace || []);"));
            Allure.addAttachment("Cart update trace", trace);
        } catch (WebDriverException e) {
            Allure.addAttachment("Cart update trace unavailable", e.getClass().getSimpleName());
        }
    }
}
