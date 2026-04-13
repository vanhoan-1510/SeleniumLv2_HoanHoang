package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class BaseElement implements Element {

    protected By locator;
    protected WebElement element;

    public BaseElement(By locator) {
        this.locator = locator;
    }

    protected WebElement getElement() {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement getClickableElement() {
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    private WebDriverWait getWait() {
        return new WebDriverWait(
            DriverManager.getDriver(),
            Duration.ofSeconds(ConfigLoader.getConfig().getTimeout())
        );
    }

    @Override
    public void click() {
        getClickableElement().click();
    }

    @Override
    public void type(String text) {
        WebElement input = getElement();
        input.clear();
        input.sendKeys(text);
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