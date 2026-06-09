package com.hoanhoang.pages;

import org.openqa.selenium.By;

import java.util.List;

public class ShoppingCartPage extends BasePage {

    By productName = By.cssSelector("a.product-title");
    By productPrice = By.cssSelector("td.product-price span.woocommerce-Price-amount");
    By productQuantityField = By.xpath("//input[contains(@class,'qty') and @type='number']");
    By quantityMinusButton = By.cssSelector(".minus");
    By quantityPlusButton = By.cssSelector(".plus");
    By checkoutButton = By.cssSelector("a.checkout-button");
    By clearCartButton = By.xpath("//a[contains(normalize-space(),'remove all items') or contains(normalize-space(),'clear cart')]");
    By emptyCartMessage = By.xpath("//h1[contains(text(),'YOUR SHOPPING CART IS EMPTY') or contains(text(),'Cart is empty')]");
    By updateCartButton = By.cssSelector("[name='update_cart']");
    By subTotalPrice = By.cssSelector("td.product-subtotal span.woocommerce-Price-amount");
    By removeProductButton = By.xpath("//a[contains(@class,'remove-item')]");

    public void removeAllProducts() {
        int count = $$(By.xpath("//a[contains(@class,'remove')]")).size();
        while (count > 0) {
            $(removeProductButton).jsClick();
            count--;
        }
    }

    public String getProductName() {
        return $(productName).getText();
    }

    public String getProductPrice() {
        return $(productPrice).getText();
    }

    public String getProductQuantityField() {
        return $(productQuantityField).getValue();
    }

    public void verifyProductDisplayed(String expectedName, String expectedPrice, String expectedQuantity) {
        verifyWithRetry(() -> {
            $(productName).shouldContainText(expectedName);
            $(productPrice).shouldHaveText(expectedPrice);
            $(productQuantityField).shouldHaveValue(expectedQuantity);
        });
    }

    public void verifyProductName(String expectedName) {
        verifyWithRetry(() -> $(productName).shouldContainText(expectedName));
    }

    public void verifyProductPrice(String expectedPrice) {
        verifyWithRetry(() -> $(productPrice).shouldHaveText(expectedPrice));
    }

    public void verifyProductQuantity(String expectedQuantity) {
        verifyWithRetry(() -> $(productQuantityField).shouldHaveValue(expectedQuantity));
    }

    public void verifyProductInCart() {
        verifyWithRetry(() -> $(productName).shouldBeVisible());
    }

    public void clickQuantityMinusButton() {
        $(quantityMinusButton).click();
    }

    public void clickQuantityPlusButton() {
        $(quantityPlusButton).click();
    }

    public void clickCheckoutButton() {
        $(checkoutButton).click();
    }

    public List<String> getAllProductNames() {
        return $$(productName).getTexts();
    }

    public List<String> getAllProductPrices() {
        return $$(productPrice).getTexts();
    }

    public void clearCart() {
        $(clearCartButton).click();
    }

    public boolean isEmptyCartMessageVisible() {
        return $(emptyCartMessage).isVisible();
    }

    public void verifyEmptyCartMessageVisible() {
        verifyWithRetry(() -> $(emptyCartMessage).shouldBeVisible());
    }

    public void verifyCartCleared() {
        verifyWithRetry(() -> $(emptyCartMessage).shouldBeVisible());
    }

    public String getSubTotalPrice() {
        return $(subTotalPrice).getText();
    }

    public void verifySubTotalPrice(String expectedPrice) {
        verifyWithRetry(() -> $(subTotalPrice).shouldHaveText(expectedPrice));
    }

    public void setProductQuantity(String quantity) {
        $(productQuantityField).type(quantity);
    }

    public void clickUpdateCartButton() {
        $(updateCartButton).click();
    }
}
