package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.pages.*;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.Map;

public class TC01 extends BaseTest {

    @Test
    public void tc1_addRandomProductToCart(ITestContext context) {
        TestDataManager.UserCredentials user = getAssignedAccount();

        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        ElectronicPage electronicPage = new ElectronicPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        homePage.goToLoginPage();
        loginPage.login(user.getUsername(), user.getPassword());

        electronicPage.goToCategoryElectronicPage();

        Map<String, String> product = electronicPage.addRandomProductToCart();

        electronicPage.goToCartPage();

        shoppingCartPage.verifyProductName(product.get("name"));
        shoppingCartPage.verifyProductPrice(product.get("price"));
        shoppingCartPage.verifyProductQuantity("1");

        shoppingCartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();
        checkoutPage.verifyOrderProductName(product.get("name"));
        checkoutPage.verifyOrderProductPrice(product.get("price"));

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();

        checkoutPage.fillBillingDetails(billingInfo);
        checkoutPage.clickPlaceOrder();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
        orderSuccessPage.verifyBillingName(billingInfo.getFirstName(), billingInfo.getLastName());
        orderSuccessPage.verifyOrderedProductName(product.get("name"));
        orderSuccessPage.verifyOrderedProductPrice(product.get("price"));
    }
}
