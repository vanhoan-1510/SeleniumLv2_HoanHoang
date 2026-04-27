package com.hoanhoang.pages;

import com.hoanhoang.data.TestDataManager;
import org.openqa.selenium.By;

import java.util.Map;

public class CheckoutPage extends BasePage {

    By checkoutHeading = By.xpath("//a[contains(@href,'checkout') and contains(@class,'active')]");
    By billingFirstName = By.cssSelector("#billing_first_name");
    By billingLastName = By.cssSelector("#billing_last_name");
    By billingAddress = By.cssSelector("#billing_address_1");
    By billingCity = By.cssSelector("#billing_city");
    By billingPostcode = By.cssSelector("#billing_postcode");
    By billingPhone = By.cssSelector("#billing_phone");
    By orderProductName = By.cssSelector("td.product-name");
    By orderProductQty = By.cssSelector(".product-quantity");
    By orderTotalPrice = By.cssSelector("tr.order-total span.woocommerce-Price-amount");
    By paymentBankTransfer = By.cssSelector("#payment_method_bacs");
    By paymentCheque = By.cssSelector("#payment_method_cheque");
    By paymentCOD = By.cssSelector("#payment_method_cod");
    By placeOrderBtn = By.cssSelector("#place_order");

    public boolean isCheckoutHeadingVisible() {
        return $(checkoutHeading).isVisible();
    }

    public void verifyCheckoutHeadingVisible() {
        $(checkoutHeading).shouldBeVisible();
    }

    public void fillBillingInfo(String firstName, String lastName, String address, String city, String postcode, String phone) {
        $(billingFirstName).type(firstName);
        $(billingLastName).type(lastName);
        $(billingAddress).type(address);
        $(billingCity).type(city);
        $(billingPostcode).type(postcode);
        $(billingPhone).type(phone);
    }

    public void fillBillingDetails(TestDataManager.BillingDetails billingInfo) {
        $(billingFirstName).type(billingInfo.getFirstName());
        $(billingLastName).type(billingInfo.getLastName());
        $(billingAddress).type(billingInfo.getAddress());
        $(billingCity).type(billingInfo.getCity());
        $(billingPostcode).type(billingInfo.getPostcode());
        $(billingPhone).type(billingInfo.getPhone());
    }

    public void selectPaymentCOD() {
        $(paymentCOD).jsClick();
    }

    public void clickPlaceOrder() {
        $(placeOrderBtn).click();
    }

    public String getOrderProductName() {
        return $(orderProductName).getText();
    }

    public String getOrderProductPrice() {
        return $(orderTotalPrice).getText();
    }
}