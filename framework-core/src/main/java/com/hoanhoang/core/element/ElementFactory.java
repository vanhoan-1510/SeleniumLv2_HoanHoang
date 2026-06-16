package com.hoanhoang.core.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ElementFactory {

    public static Element $(By locator) {
        return new BaseElement(locator);
    }

    public static Element $(WebElement element) {
        return new BaseElement(element);
    }
}
