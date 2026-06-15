package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.*;
import org.testng.annotations.Test;

import java.util.Map;

public class TC01 extends BaseTest {

    @Test
    public void tc1_addRandomProductToCart() {
        TestDataManager.UserCredentials user = getAssignedAccount();

        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        ElectronicPage electronicPage = new ElectronicPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        header.goToLoginPage();
        loginPage.login(user.getUsername(), user.getPassword());

        header.goToCartPage();
        shoppingCartPage.removeAllProducts();

        header.goToShopPage();
        header.goToCategoryElectronicPage();

        Map<String, String> product = electronicPage.addRandomProductToCart();

        header.goToCartPage();

        shoppingCartPage.verifyProductName(product.get("name"));
        shoppingCartPage.verifyProductPrice(product.get("price"));
        shoppingCartPage.verifyProductQuantity("1");

        shoppingCartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();
        checkoutPage.verifyOrderProductName(product.get("name"));
        checkoutPage.verifyOrderProductPrice(product.get("price"));

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();

        checkoutPage.fillBillingDetails(billingInfo);
        checkoutPage.selectPaymentMethod(PaymentMethod.COD);
        checkoutPage.clickPlaceOrderExpectingSuccess();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
        orderSuccessPage.verifyOrderConfirmationMessage();
        orderSuccessPage.verifyBillingName(billingInfo.getFirstName(), billingInfo.getLastName());
        orderSuccessPage.verifyOrderedProductName(product.get("name"));
        orderSuccessPage.verifyOrderedProductPrice(product.get("price"));
    }
}
