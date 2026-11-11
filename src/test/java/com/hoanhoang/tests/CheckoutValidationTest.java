package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.builder.BillingBuilder;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import com.hoanhoang.pages.components.BillingFormComponent.Field;
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

        List<Field> expectedMissingFields =
                new ArrayList<>(
                        List.of(
                                Field.FIRST_NAME,
                                Field.LAST_NAME,
                                Field.ADDRESS,
                                Field.CITY,
                                Field.PHONE,
                                Field.EMAIL));
        if (checkout.isPostcodeRequired()) {
            expectedMissingFields.add(Field.POSTCODE);
        }
        checkout.fillBillingDetails(BillingBuilder.blank().build());
        checkout.placeOrder();

        assertRequiredFieldValidation(checkout, expectedMissingFields);
    }

    private void assertRequiredFieldValidation(
            CheckoutPage checkout, List<Field> expectedMissingFields) {
        assertThat(checkout.isLoaded()).as("Validation keeps user at checkout").isTrue();
        assertThat(checkout.getErrorMessages())
                .as("Required-field validation messages")
                .isNotEmpty()
                .allSatisfy(message -> assertThat(message).isNotBlank());
        assertThat(checkout.getFieldErrorMessages())
                .as("Missing mandatory fields have validation messages")
                .containsKeys(expectedMissingFields.toArray(Field[]::new));
        assertThat(checkout.getInvalidBillingFields())
                .as("Missing mandatory fields highlighted")
                .containsAll(expectedMissingFields);
    }
}
