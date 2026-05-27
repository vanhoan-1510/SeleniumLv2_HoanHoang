package com.hoanhoang.core.element;

import org.openqa.selenium.By;

public class ElementsFactory {

    public static Elements $$(By locator) {
        return new BaseElements(locator);
    }
}