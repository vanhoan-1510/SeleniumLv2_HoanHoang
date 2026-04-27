package com.hoanhoang.core.element;

import org.openqa.selenium.By;

public class ElementFactory{



    public static Element $(By locator) {
        return new BaseElement(locator);
    }
}