package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.builder.BillingBuilder;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import org.testng.annotations.Test;

public class TC07 extends BaseTest {

    @Test(description = "TC07 - Ensure proper error handling when mandatory fields are blank")
    public void checkoutShowsErrorsForBlankMandatoryFields() {
        new HomePage().open();
        HeaderComponent header = new HeaderComponent();
        header.goToShopPage();
        new ShopPage().addFirstProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();

        cart.clickCheckoutButton();
        CheckoutPage checkout = new CheckoutPage();
        checkout.waitUntilLoaded();

        checkout.fillBillingDetails(BillingBuilder.blank().build());
        checkout.placeOrder();

        assertRequiredFieldValidation(checkout);
    }

    private void assertRequiredFieldValidation(CheckoutPage checkout) {
        assertThat(checkout.isLoaded()).as("Validation keeps user at checkout").isTrue();
        assertThat(checkout.getErrorMessages())
                .as("Required-field validation messages")
                .isNotEmpty()
                .allSatisfy(message -> assertThat(message).isNotBlank());
        assertThat(checkout.getFieldErrors())
                .as("Missing mandatory fields highlighted")
                .containsKeys(
                        "billing_first_name",
                        "billing_last_name",
                        "billing_address_1",
                        "billing_city",
                        "billing_phone",
                        "billing_email");
    }
}
