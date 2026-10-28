package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.core.util.UiWait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class BasePage {
    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }

    protected Elements $$(By locator) {
        return ElementFactory.$$(locator);
    }

    /** Checks (with auto-retry) that the browser title equals the expected title. */
    public void haveTitle(String expected) {
        UiWait.assertEventually(
                "page title should be '" + expected + "'", WebDriver::getTitle, expected::equals);
    }
}
