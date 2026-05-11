package com.hoanhoang.core.element;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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

    private static WebDriver driver() {
        return DriverManager.getDriver();
    }

    protected WebElement getElement() {
        WebElement el = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        if (!el.isDisplayed()) {
            el = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
        return el;
    }

    protected WebElement getClickableElement() {
        WebElement el = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        if (!el.isDisplayed() || !el.isEnabled()) {
            el = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            el = getWait().until(ExpectedConditions.elementToBeClickable(locator));
        }
        return el;
    }

    private WebDriverWait getWait() {
        return new WebDriverWait(
            driver(),
            Duration.ofSeconds(ConfigLoader.getConfig().getTimeout())
        );
    }

    public static int count(String xpath) {
        return driver().findElements(By.xpath(xpath)).size();
    }

    @Override
    public void click() {
        WebElement el = getClickableElement();
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        el.click();
    }

    @Override
    public void jsClick() {
        WebElement el = getElement();
        ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", el);
    }

    @Override
    public void type(String text) {
        WebElement el = getElement();
        el.clear();
        el.sendKeys(text);
    }

    @Override
    public void shouldHaveText(String expected) {
        Assert.assertEquals(getElement().getText(), expected);
    }

    @Override
    public void shouldHaveValue(String expected) {
        Assert.assertEquals(getClickableElement().getAttribute("value"), expected);
    }

    @Override
    public void shouldHave(String prop, String expected) {
        Assert.assertEquals(getElement().getAttribute(prop), expected);
    }

    @Override
    public String getText() {
        return getElement().getText();
    }

    @Override
    public String getValue() {
        return getClickableElement().getAttribute("value");
    }

    @Override
    public void hover() {
        WebElement el = getElement();
        new Actions(driver())
                .moveToElement(el)
                .pause(Duration.ofMillis(500))
                .perform();
    }

    @Override
    public boolean isVisible() {
        try {
            getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void shouldBeVisible() {
        boolean visible = false;
        try {
            getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            visible = true;
        } catch (Exception e) {
        }
        Assert.assertTrue(visible, "Element is NOT visible: " + locator.toString());
    }
}
