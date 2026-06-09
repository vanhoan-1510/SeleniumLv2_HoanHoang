package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.*;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.Map;

public class TC03 extends BaseTest {

    @Test
    public void tc3_buyItemWithDifferentPaymentMethods(ITestContext context) {
        PaymentMethod[] paymentMethods = PaymentMethod.values();

        for (PaymentMethod paymentMethod : paymentMethods) {
            runTestWithPaymentMethod(context, paymentMethod);
        }
    }

    private void runTestWithPaymentMethod(ITestContext context, PaymentMethod paymentMethod) {
        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        if (paymentMethod != PaymentMethod.values()[0]) {
            header.goToShopPage();
        } else {
            header.goToLoginPage();
            loginPage.login(getAssignedAccount().getUsername(), getAssignedAccount().getPassword());
        }

        header.goToCartPage();
        shoppingCartPage.removeAllProducts();
        header.goToShopPage();

        Map<String, String> product = shopPage.addMultipleRandomProductsToCart().get(0);

        header.goToCartPage();
        shoppingCartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();

        checkoutPage.fillBillingDetails(TestDataManager.getBillingInfo());
        checkoutPage.selectPaymentMethod(paymentMethod);

        checkoutPage.clickPlaceOrder();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
        orderSuccessPage.verifyOrderConfirmationMessage();

        orderSuccessPage.verifyOrderedProductName(product.get("name"));

        String paymentMethodText = orderSuccessPage.getPaymentMethod();
        if (paymentMethodText == null) {
            throw new AssertionError("Payment method should be displayed");
        }

        header.goToShopPage();
    }
}
