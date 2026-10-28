package com.hoanhoang.tests;

import static com.hoanhoang.tests.support.PurchaseAssertions.assertCart;
import static com.hoanhoang.tests.support.PurchaseAssertions.assertOrderItems;
import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.ElectronicPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.OrderSuccessPage;
import com.hoanhoang.pages.ShoppingCartPage;
import java.util.List;
import org.testng.annotations.Test;

public class TC01 extends BaseTest {

    @Test(description = "TC01 - Verify users can buy an item successfully")
    public void userCanBuyAnItem() {
        new HomePage().open();
        login();
        openShopWithEmptyCart();

        HeaderComponent header = new HeaderComponent();
        header.goToCategoryElectronicPage();
        ElectronicPage electronic = new ElectronicPage();
        electronic.haveTitle(ElectronicPage.TITLE);
        Product expectedProduct = electronic.addRandomProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        assertCart(cart.getProducts(), List.of(expectedProduct));

        cart.clickCheckoutButton();
        CheckoutPage checkout = new CheckoutPage();
        checkout.waitUntilLoaded();
        assertThat(checkout.isLoaded()).as("Checkout page displayed").isTrue();
        assertOrderItems(checkout.getProducts(), List.of(expectedProduct));

        BillingDetails billing = TestDataManager.getBillingInfo();
        checkout.fillBillingDetails(billing);
        String defaultPayment = checkout.getSelectedPaymentMethod();
        checkout.placeOrder();

        OrderSuccessPage success = new OrderSuccessPage();
        success.waitUntilLoaded();
        assertThat(success.isLoaded()).as("Order status page displayed").isTrue();
        success.confirmationMessage().shouldHaveText("Thank you");
        assertOrderItems(success.getProducts(), List.of(expectedProduct));
        assertBillingDetails(success, billing);
        assertThat(success.getPaymentMethod())
                .as("Default payment retained")
                .isEqualToIgnoringCase(defaultPayment);
    }

    private void assertBillingDetails(OrderSuccessPage success, BillingDetails billing) {
        assertThat(success.getBillingAddress())
                .as("Billing details")
                .contains(
                        billing.firstName(),
                        billing.lastName(),
                        billing.address(),
                        billing.city(),
                        billing.phone(),
                        billing.email());
    }
}
