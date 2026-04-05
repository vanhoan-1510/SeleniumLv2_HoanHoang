package com.hoanhoang.core.element;

import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class BaseElement implements Element {

    protected By locator;
    protected WebElement element;

    public BaseElement(By locator) {
        this.locator = locator;
    }

    protected WebElement getElement() {
        return DriverManager.getDriver().findElement(locator);
    }

    @Override
    public void click() {
        getElement().click();
    }

    @Override
    public void type(String text) {
        getElement().clear();
        getElement().sendKeys(text);
    }

    @Override
    public void shouldHaveText(String expected) {
        Assert.assertEquals(getElement().getText(), expected);
    }

    @Override
    public void shouldHaveValue(String expected) {
        Assert.assertEquals(getElement().getAttribute("value"), expected);
    }

    @Override
    public void shouldHave(String prop, String expected) {
        Assert.assertEquals(getElement().getAttribute(prop), expected);
    }
}