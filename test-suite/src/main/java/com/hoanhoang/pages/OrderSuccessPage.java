package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class OrderSuccessPage extends BasePage {

    By statusMessage = By.xpath("//p[contains(@class,'thankyou-order-received')]");
    By orderNumber = By.xpath("//li[contains(.,'Order number')]//strong");
    By orderDate = By.xpath("//li[contains(.,'Date')]//strong");
    By orderTotal = By.xpath("//li[contains(.,'Total')]//strong");
    By orderPaymentMethod = By.xpath("//tr[th[normalize-space()='Payment method:']]/td");
    By orderProductName = By.xpath("//td[contains(@class,'product-name')]/a");
    By orderProductQty = By.xpath("//td[contains(@class,'product-name')]//strong");
    By orderProductPrice = By.cssSelector("//td[contains(@class,'product-total')]//span");
    By billingName = By.xpath("//h2[normalize-space()='Billing address']/following-sibling::address");

    public boolean isOrderSuccessHeadingVisible() {
        return $(statusMessage).isVisible();
    }

    public void verifyOrderSuccessHeadingVisible() {
        $(statusMessage).shouldBeVisible();
    }

    public void verifyOrderConfirmationMessage() {
        $(statusMessage).shouldContainText("Thank you");
    }

    public String getOrderedProductName() {
        return $(orderProductName).getText();
    }

    public String getOrderedProductPrice() {
        return $(orderProductPrice).getText();
    }

    public String getBillingName() {
        return $(billingName).getText();
    }

    public void verifyOrderedProduct(String expectedName, String expectedPrice) {
        $(orderProductName).shouldHaveText(expectedName);
        $(orderProductPrice).shouldHaveText(expectedPrice);
    }

    public void verifyOrderedProductName(String expectedName) {
        $(orderProductName).shouldHaveText(expectedName);
    }

    public void verifyOrderedProductPrice(String expectedPrice) {
        $(orderProductPrice).shouldHaveText(expectedPrice);
    }

    public void verifyBillingName(String expectedFirstName, String expectedLastName) {
        $(billingName).shouldContainText(expectedFirstName);
        $(billingName).shouldContainText(expectedLastName);
    }

    public void verifyOrderDetailsContain(String expectedName, String expectedPrice, String expectedFirstName, String expectedLastName) {
        verifyOrderedProductName(expectedName);
        verifyOrderedProductPrice(expectedPrice);
        verifyBillingName(expectedFirstName, expectedLastName);
    }

    public String getStatusMessage() {
        return $(statusMessage).getText();
    }

    public String getOrderNumber() {
        return $(orderNumber).getText();
    }

    public String getOrderDate() {
        return $(orderDate).getText();
    }

    public String getOrderTotal() {
        return $(orderTotal).getText();
    }

    public String getPaymentMethod() {
        return $(orderPaymentMethod).getText();
    }

    public String getOrderProductQty() {
        return $(orderProductQty).getText();
    }
}