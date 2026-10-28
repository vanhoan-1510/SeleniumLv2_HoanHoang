package com.hoanhoang.core.element;

import org.openqa.selenium.By;

public final class ElementFactory {

    private ElementFactory() {}

    public static Element $(By locator) {
        return new BaseElement(locator);
    }

    public static Elements $$(By locator) {
        return new BaseElements(locator);
    }
}
