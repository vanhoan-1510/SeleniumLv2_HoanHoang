package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.RetryHandler;
import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class CheckoutPage extends BasePage {

    By checkoutHeading = By.cssSelector("form.checkout, #customer_details, #place_order");
    By billingFirstName = By.cssSelector("#billing_first_name");
    By billingLastName = By.cssSelector("#billing_last_name");
    By billingAddress = By.cssSelector("#billing_address_1");
    By billingCity = By.cssSelector("#billing_city");
    By billingPostcode = By.cssSelector("#billing_postcode");
    By billingPhone = By.cssSelector("#billing_phone");
    By billingEmail = By.cssSelector("#billing_email");
    By orderProductName = By.cssSelector("#order_review td.product-name");
    By orderProductPrice = By.cssSelector("#order_review td.product-total span.woocommerce-Price-amount");
    By orderTotalPrice = By.cssSelector("#order_review tr.order-total span.woocommerce-Price-amount");
    By paymentContainer = By.cssSelector("#payment, ul.payment_methods");
    By paymentBankTransfer = By.cssSelector("#payment_method_bacs");
    By paymentCheque = By.cssSelector("#payment_method_cheque");
    By paymentCOD = By.cssSelector("#payment_method_cod");
    By placeOrderBtn = By.cssSelector("#place_order");
    By orderSuccessMessage = By.xpath("//p[contains(@class,'thankyou-order-received')]");
    By errorMessage = By.cssSelector(".woocommerce-NoticeGroup-checkout .woocommerce-error li, .woocommerce-error li");
    By firstNameError = By.cssSelector("li[data-id='billing_first_name']");
    By lastNameError = By.cssSelector("li[data-id='billing_last_name']");
    By addressError = By.cssSelector("li[data-id='billing_address_1']");
    By cityError = By.cssSelector("li[data-id='billing_city']");
    By postcodeError = By.cssSelector("li[data-id='billing_postcode']");
    By phoneError = By.cssSelector("li[data-id='billing_phone']");

    public boolean isCheckoutHeadingVisible() {
        return $(checkoutHeading).isVisible();
    }

    public void verifyCheckoutHeadingVisible() {
        $(checkoutHeading).shouldBeVisible();
    }

    public void fillBillingInfo(String firstName, String lastName, String address, String city, String postcode, String phone) {
        fillBillingInfo(firstName, lastName, address, city, postcode, phone, "john.doe@example.com");
    }

    public void fillBillingInfo(String firstName, String lastName, String address, String city, String postcode, String phone, String email) {
        $(billingFirstName).type(firstName);
        $(billingLastName).type(lastName);
        $(billingAddress).type(address);
        $(billingCity).type(city);
        $(billingPostcode).type(postcode);
        $(billingPhone).type(phone);
        fillBillingEmailIfVisible(email);
    }

    public void fillBillingDetails(TestDataManager.BillingDetails billingInfo) {
        $(billingFirstName).type(billingInfo.getFirstName());
        $(billingLastName).type(billingInfo.getLastName());
        $(billingAddress).type(billingInfo.getAddress());
        $(billingCity).type(billingInfo.getCity());
        $(billingPostcode).type(billingInfo.getPostcode());
        $(billingPhone).type(billingInfo.getPhone());
        fillBillingEmailIfVisible("john.doe@example.com");
    }

    private void fillBillingEmailIfVisible(String email) {
        if ($(billingEmail).isVisible()) {
            $(billingEmail).type(email);
        }
    }

    public void selectPaymentMethod(PaymentMethod paymentMethod) {
        By locator = getPaymentMethodLocator(paymentMethod);
        RetryHandler.retryUntilTimeout(() -> {
            waitForCheckoutReady();
            findPresentElement(paymentContainer, "Payment methods are not loaded yet");
            WebElement paymentOption = findPresentElement(locator, "Payment method is not present: " + paymentMethod);
            if (!paymentOption.isEnabled()) {
                throw new AssertionError("Payment method is disabled: " + paymentMethod);
            }

            ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();",
                    paymentOption
            );

            Object checked = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                    "return arguments[0].checked === true;",
                    paymentOption
            );
            if (!Boolean.TRUE.equals(checked)) {
                throw new AssertionError("Payment method was not selected: " + paymentMethod);
            }
        }, getSubmitResultTimeoutMs(), ConfigLoader.getConfig().getStepRetryIntervalMs());
    }

    private By getPaymentMethodLocator(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case BANK_TRANSFER:
                return paymentBankTransfer;
            case CHEQUE:
                return paymentCheque;
            case COD:
                return paymentCOD;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }

    public void clickPlaceOrder() {
        clickPlaceOrderButton();
        waitForPlaceOrderResult(false);
    }

    public void clickPlaceOrderExpectingSuccess() {
        clickPlaceOrderButton();
        waitForPlaceOrderResult(true);
    }

    private void clickPlaceOrderButton() {
        RetryHandler.retryUntilTimeout(() -> {
            waitForCheckoutReady();
            WebElement button = findVisibleElement(placeOrderBtn, "Place order button is not visible");
            ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
            if (!button.isEnabled()) {
                throw new AssertionError("Place order button is disabled");
            }
            button.click();
        }, ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(), ConfigLoader.getConfig().getStepRetryIntervalMs());
    }

    public String getOrderProductName() {
        return $(orderProductName).getText();
    }

    public String getOrderProductPrice() {
        return $(orderTotalPrice).getText();
    }

    public void verifyOrderProduct(String expectedName, String expectedPrice) {
        verifyOrderProductName(expectedName);
        verifyOrderProductPrice(expectedPrice);
    }

    public void verifyOrderProductName(String expectedName) {
        verifyWithRetry(() -> {
            List<String> actualNames = $$(orderProductName).getTexts();
            boolean found = actualNames.stream().anyMatch(name -> name.toLowerCase().contains(expectedName.toLowerCase()));
            if (!found) {
                throw new AssertionError("Checkout should contain product name: " + expectedName + ". Actual: " + actualNames);
            }
        });
    }

    public void verifyOrderProductPrice(String expectedPrice) {
        verifyWithRetry(() -> {
            List<String> actualPrices = $$(orderProductPrice).getTexts();
            if (!actualPrices.contains(expectedPrice)) {
                throw new AssertionError("Checkout should contain product price: " + expectedPrice + ". Actual: " + actualPrices);
            }
        });
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
        $(firstNameError).shouldBeVisible();
        $(lastNameError).shouldBeVisible();
        $(addressError).shouldBeVisible();
        $(cityError).shouldBeVisible();
        $(phoneError).shouldBeVisible();
    }

    private void waitForPlaceOrderResult(boolean expectSuccess) {
        long deadline = System.currentTimeMillis() + getSubmitResultTimeoutMs();
        AssertionError lastError = null;

        while (System.currentTimeMillis() < deadline) {
            if (isOrderSuccessPage()) {
                return;
            }

            List<String> errors = getVisibleTexts(errorMessage);
            if (!errors.isEmpty()) {
                if (expectSuccess) {
                    throw new AssertionError("Checkout failed after placing order: " + errors);
                }
                return;
            }

            lastError = new AssertionError("Order was not submitted successfully. URL: "
                    + DriverManager.getDriver().getCurrentUrl());
            sleep(ConfigLoader.getConfig().getStepRetryIntervalMs());
        }

        throw lastError;
    }

    private long getSubmitResultTimeoutMs() {
        return Math.max(
                ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(),
                ConfigLoader.getConfig().getTestRetryTimeoutMs()
        );
    }

    private boolean isOrderSuccessPage() {
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        return currentUrl.contains("order-received")
                || DriverManager.getDriver().findElements(orderSuccessMessage).stream().anyMatch(WebElement::isDisplayed);
    }

    private void waitForCheckoutReady() {
        Object hasBlockingOverlay = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "return Array.from(document.querySelectorAll('.blockUI.blockOverlay')).some(function(overlay) {" +
                        "var style = window.getComputedStyle(overlay);" +
                        "var rect = overlay.getBoundingClientRect();" +
                        "return style.display !== 'none' && style.visibility !== 'hidden' && rect.width > 0 && rect.height > 0;" +
                        "});"
        );
        if (Boolean.TRUE.equals(hasBlockingOverlay)) {
            throw new AssertionError("Checkout is still blocked by loading overlay");
        }
    }

    private WebElement findVisibleElement(By locator, String errorMessage) {
        return DriverManager.getDriver().findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new AssertionError(errorMessage));
    }

    private WebElement findPresentElement(By locator, String errorMessage) {
        return DriverManager.getDriver().findElements(locator).stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError(errorMessage));
    }

    private List<String> getVisibleTexts(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    private void sleep(long timeoutMs) {
        try {
            Thread.sleep(timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for checkout submit result", e);
        }
    }
}
