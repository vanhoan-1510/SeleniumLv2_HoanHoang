package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import org.openqa.selenium.By;

public class BasePage {

    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }
}