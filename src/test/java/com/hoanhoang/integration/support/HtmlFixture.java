package com.hoanhoang.integration.support;

import com.hoanhoang.core.driver.DriverManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Loads a classpath HTML document into the current browser. */
public final class HtmlFixture {
    private HtmlFixture() {}

    public static void open(String path) throws IOException {
        try (var resource = HtmlFixture.class.getClassLoader().getResourceAsStream(path)) {
            if (resource == null) {
                throw new IllegalStateException("Missing HTML fixture: " + path);
            }
            String html = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            DriverManager.getDriver()
                    .get(
                            "data:text/html;charset=utf-8,"
                                    + URLEncoder.encode(html, StandardCharsets.UTF_8)
                                            .replace("+", "%20"));
        }
    }
}
