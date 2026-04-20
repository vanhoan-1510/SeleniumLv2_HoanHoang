package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class ShoppingCartPage extends BasePage {

    By productName = By.cssSelector(".product-name");
    By productPrice = By.cssSelector("td.product-price span.woocommerce-Price-amount");
    By productQuantityField = By.xpath("//input[contains(@class,'qty') and @type='number']");
    By quantityMinusButton = By.cssSelector(".minus");
    By quantityPlusButton = By.cssSelector(".plus");
    By checkoutButton = By.cssSelector("proceedToCheckout: xpath=//a[contains(normalize-space(),'checkout')]");

    public String getProductName() {
        return $(productName).getText();
    }

    public String getProductPrice() {
        return $(productPrice).getText();
    }

    public String getProductQuantityField() {
        return $(productQuantityField).getText();
    }

    public void clickQuantityButton() {
        $(quantityMinusButton).click();
    }

    public void clickQuantityPlusButton() {
        $(quantityPlusButton).click();
    }

    public void clickCheckoutButton() {
        $(checkoutButton).click();
    }
}
