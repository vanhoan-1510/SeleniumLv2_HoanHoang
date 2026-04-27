package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.pages.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

public class TC01 extends BaseTest {

    @Test
    public void tc1_addRandomProductToCart() {
        SoftAssert softAssert = new SoftAssert();
        TestDataManager.UserCredentials user = TestDataManager.getValidCredentials();

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

        electronicPage.goToProductCardPage();

        softAssert.assertEquals(shoppingCartPage.getProductName(), product.get("name"), "Product name in cart does not match the added product.");
        softAssert.assertEquals(shoppingCartPage.getProductPrice(), product.get("price"), "Product price in cart does not match the added product.");
        softAssert.assertEquals(shoppingCartPage.getProductQuantityField(), "1", "Product quantity in cart should be 1 after adding to cart.");

        shoppingCartPage.clickCheckoutButton();

        softAssert.assertTrue(checkoutPage.isCheckoutHeadingVisible(), "Checkout page should be displayed after clicking checkout button.");

        softAssert.assertEquals(checkoutPage.getOrderProductName(), product.get("name"), "Item name in checkout order does not match.");
        softAssert.assertEquals(checkoutPage.getOrderProductPrice(), product.get("price"), "Item price in checkout order does not match.");

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();

        checkoutPage.fillBillingDetails(billingInfo);

        checkoutPage.clickPlaceOrder();

        softAssert.assertTrue(orderSuccessPage.isOrderSuccessHeadingVisible(), "Order Status page should be displayed after placing order.");

        softAssert.assertTrue(orderSuccessPage.getBillingName().contains(billingInfo.getFirstName()) &&
                        orderSuccessPage.getBillingName().contains(billingInfo.getLastName()),
                "Billing name on order does not match.");

        softAssert.assertEquals(orderSuccessPage.getOrderedProductName(), product.get("name"), "Ordered item name on success page does not match.");
        softAssert.assertEquals(orderSuccessPage.getOrderedProductPrice(), product.get("price"), "Ordered item price on success page does not match.");

        softAssert.assertAll();
    }
}