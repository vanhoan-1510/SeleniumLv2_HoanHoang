package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
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
    By billingPhone = By.xpath("//h2[normalize-space()='Billing address']/following-sibling::address//span[contains(text(),'Phone')] | //h2[normalize-space()='Billing address']/following-sibling::address//li[contains(text(),'Phone')]");
    By billingEmail = By.xpath("//h2[normalize-space()='Billing address']/following-sibling::address//span[contains(text(),'Email')] | //h2[normalize-space()='Billing address']/following-sibling::address//li[contains(text(),'Email')]");

    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }

    public boolean isOrderSuccessHeadingVisible() {
        return $(statusMessage).isVisible();
    }

    public void verifyOrderSuccessHeadingVisible() {
        $(statusMessage).shouldBeVisible();
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
        String actualName = getOrderedProductName();
        String actualPrice = getOrderedProductPrice();

        if (!actualName.equals(expectedName)) {
            throw new AssertionError("Ordered product name mismatch. Expected: " + expectedName + ", Actual: " + actualName);
        }
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Ordered product price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
    }

    public void verifyOrderedProductName(String expectedName) {
        String actualName = getOrderedProductName();
        if (!actualName.equals(expectedName)) {
            throw new AssertionError("Ordered product name mismatch. Expected: " + expectedName + ", Actual: " + actualName);
        }
    }

    public void verifyOrderedProductPrice(String expectedPrice) {
        String actualPrice = getOrderedProductPrice();
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Ordered product price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
    }

    public void verifyBillingName(String expectedFirstName, String expectedLastName) {
        String actualName = getBillingName();
        if (!actualName.contains(expectedFirstName) || !actualName.contains(expectedLastName)) {
            throw new AssertionError("Billing name mismatch. Expected to contain: " + expectedFirstName + " " + expectedLastName + ", Actual: " + actualName);
        }
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

    public String getBillingPhone() {
        return $(billingPhone).getText();
    }

    public String getBillingEmail() {
        return $(billingEmail).getText();
    }
}
