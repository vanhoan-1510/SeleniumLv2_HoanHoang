package com.hoanhoang.pages;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
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
    By orderTotalPrice = By.cssSelector("tr.order-total span.woocommerce-Price-amount");
    By paymentBankTransfer = By.cssSelector("#payment_method_bacs");
    By paymentCheque = By.cssSelector("#payment_method_cheque");
    By paymentCOD = By.cssSelector("#payment_method_cod");
    By placeOrderBtn = By.cssSelector("#place_order");
    By errorMessage = By.xpath("//div[contains(@class,'NoticeGroup')]//li");
    By firstNameError = By.cssSelector("li[data-id='billing_first_name']");
    By lastNameError = By.cssSelector("li[data-id='billing_last_name']");
    By addressError = By.cssSelector("li[data-id='billing_address_1']");
    By cityError = By.cssSelector("li[data-id='billing_city']");
    By postcodeError = By.cssSelector("");
    By phoneError = By.cssSelector("li[data-id='billing_phone']");

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

    public void selectPaymentMethod(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case BANK_TRANSFER:
                $(paymentBankTransfer).click();
                break;
            case CHEQUE:
                $(paymentCheque).click();
                break;
            case COD:
                $(paymentCOD).click();
                break;
        }
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

    public void verifyOrderProduct(String expectedName, String expectedPrice) {
        String actualName = getOrderProductName();
        String actualPrice = getOrderProductPrice();

        if (!actualName.equals(expectedName)) {
            throw new AssertionError("Order product name mismatch. Expected: " + expectedName + ", Actual: " + actualName);
        }
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Order product price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
    }

    public void verifyOrderProductName(String expectedName) {
        String actualName = getOrderProductName();
        if (!actualName.equals(expectedName)) {
            throw new AssertionError("Order product name mismatch. Expected: " + expectedName + ", Actual: " + actualName);
        }
    }

    public void verifyOrderProductPrice(String expectedPrice) {
        String actualPrice = getOrderProductPrice();
        if (!actualPrice.equals(expectedPrice)) {
            throw new AssertionError("Order product price mismatch. Expected: " + expectedPrice + ", Actual: " + actualPrice);
        }
    }

    public boolean isErrorMessageDisplayed() {
        return $(errorMessage).isVisible();
    }

    public void verifyErrorMessageDisplayed() {
        $(errorMessage).shouldBeVisible();
    }

    public String getErrorMessageText() {
        return $(errorMessage).getText();
    }

    public boolean isFirstNameErrorVisible() {
        return $(firstNameError).isVisible();
    }

    public boolean isLastNameErrorVisible() {
        return $(lastNameError).isVisible();
    }

    public boolean isAddressErrorVisible() {
        return $(addressError).isVisible();
    }

    public boolean isCityErrorVisible() {
        return $(cityError).isVisible();
    }

    public boolean isPostcodeErrorVisible() {
        return $(postcodeError).isVisible();
    }

    public boolean isPhoneErrorVisible() {
        return $(phoneError).isVisible();
    }

    public void verifyRequiredFieldErrorsVisible() {
        if (!isFirstNameErrorVisible() || !isLastNameErrorVisible() ||
            !isAddressErrorVisible() || !isCityErrorVisible() ||
            !isPostcodeErrorVisible() || !isPhoneErrorVisible()) {
            throw new AssertionError("All required field error messages should be visible");
        }
    }
}