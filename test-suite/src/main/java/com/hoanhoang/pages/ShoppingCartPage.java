package com.hoanhoang.pages;

import com.hoanhoang.core.element.BaseElement;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartPage extends BasePage {

    By productName = By.cssSelector(".product-title");
    By productPrice = By.cssSelector("td.product-price span.woocommerce-Price-amount");
    By productQuantityField = By.xpath("//input[contains(@class,'qty') and @type='number']");
    By quantityMinusButton = By.cssSelector(".minus");
    By quantityPlusButton = By.cssSelector(".plus");
    By checkoutButton = By.xpath("//a[contains(normalize-space(),'checkout')]");
    By productNameList = By.cssSelector(".product-name");
    By productPriceList = By.cssSelector("td.product-price span.woocommerce-Price-amount");
    By clearCartButton = By.xpath("//a[contains(normalize-space(),'remove all items') or contains(normalize-space(),'clear cart')]");
    By emptyCartMessage = By.xpath("//h1[contains(text(),'YOUR SHOPPING CART IS EMPTY') or contains(text(),'Cart is empty')]");
    By updateCartButton = By.cssSelector("[name='update_cart']");
    By subTotalPrice = By.cssSelector("td.product-subtotal span.woocommerce-Price-amount");
    By removeProductButton = By.cssSelector("a.remove");

    public void removeAllProducts() {
        int count = BaseElement.count("//a[contains(@class,'remove')]");
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
        String actualName = getProductName();
        String actualPrice = getProductPrice();
        String actualQuantity = getProductQuantityField();

        if (!actualName.equals(expectedName)) {
            throw new AssertionError("Product name mismatch. Expected: " + expectedName + ", Actual: " + actualName);
        }
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Product price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
        if (!actualQuantity.equals(expectedQuantity)) {
            throw new AssertionError("Product quantity mismatch. Expected: " + expectedQuantity + ", Actual: " + actualQuantity);
        }
    }

    public void verifyProductName(String expectedName) {
        String actualName = getProductName();
        if (!actualName.equals(expectedName)) {
            throw new AssertionError("Product name mismatch. Expected: " + expectedName + ", Actual: " + actualName);
        }
    }

    public void verifyProductPrice(String expectedPrice) {
        String actualPrice = getProductPrice();
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Product price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
    }

    public void verifyProductQuantity(String expectedQuantity) {
        String actualQuantity = getProductQuantityField();
        if (!actualQuantity.equals(expectedQuantity)) {
            throw new AssertionError("Product quantity mismatch. Expected: " + expectedQuantity + ", Actual: " + actualQuantity);
        }
    }

    public void verifyProductInCart() {
        String name = getProductName();
        if (name == null || name.isEmpty()) {
            throw new AssertionError("Product should be displayed in cart");
        }
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

    public List<String> getAllProductNames() {
        List<String> names = new ArrayList<>();
        int count = BaseElement.count("//td[contains(@class,'product-name')]");
        for (int i = 1; i <= count; i++) {
            String xpath = String.format("(//td[contains(@class,'product-name')])[%s]", i);
            names.add(new BaseElement(By.xpath(xpath)).getText());
        }
        return names;
    }

    public List<String> getAllProductPrices() {
        List<String> prices = new ArrayList<>();
        int count = BaseElement.count("//td[contains(@class,'product-price')]//span[contains(@class,'amount')]");
        for (int i = 1; i <= count; i++) {
            String xpath = String.format("(//td[contains(@class,'product-price')]//span[contains(@class,'amount')])[%s]", i);
            prices.add(new BaseElement(By.xpath(xpath)).getText());
        }
        return prices;
    }

    public void clearCart() {
        $(clearCartButton).click();
    }

    public boolean isEmptyCartMessageVisible() {
        return $(emptyCartMessage).isVisible();
    }

    public void verifyEmptyCartMessageVisible() {
        $(emptyCartMessage).shouldBeVisible();
    }

    public void verifyCartCleared() {
        if (!isEmptyCartMessageVisible()) {
            throw new AssertionError("Empty cart message should be displayed after clearing cart");
        }
    }

    public String getSubTotalPrice() {
        return $(subTotalPrice).getText();
    }

    public void verifySubTotalPrice(String expectedPrice) {
        String actualPrice = getSubTotalPrice();
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Subtotal price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
    }

    public void setProductQuantity(String quantity) {
        $(productQuantityField).type(quantity);
    }

    public void clickUpdateCartButton() {
        $(updateCartButton).click();
    }
}
