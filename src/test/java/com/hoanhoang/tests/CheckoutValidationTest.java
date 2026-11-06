package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.builder.BillingBuilder;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

public class CheckoutValidationTest extends BaseTest {

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

        List<String> expectedMissingFields =
                new ArrayList<>(
                        List.of(
                                "billing_first_name",
                                "billing_last_name",
                                "billing_address_1",
                                "billing_city",
                                "billing_phone",
                                "billing_email"));
        if (checkout.isPostcodeRequired()) {
            expectedMissingFields.add("billing_postcode");
        }
        checkout.fillBillingDetails(BillingBuilder.blank().build());
        checkout.placeOrder();

        assertRequiredFieldValidation(checkout, expectedMissingFields);
    }

    private void assertRequiredFieldValidation(
            CheckoutPage checkout, List<String> expectedMissingFields) {
        assertThat(checkout.isLoaded()).as("Validation keeps user at checkout").isTrue();
        assertThat(checkout.getErrorMessages())
                .as("Required-field validation messages")
                .isNotEmpty()
                .allSatisfy(message -> assertThat(message).isNotBlank());
        assertThat(checkout.getFieldErrorMessages())
                .as("Missing mandatory fields have validation messages")
                .containsKeys(expectedMissingFields.toArray(String[]::new));
        assertThat(checkout.getInvalidBillingFieldIds())
                .as("Missing mandatory fields highlighted")
                .containsAll(expectedMissingFields);
    }
}
