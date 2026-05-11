package com.hoanhoang.tests;

import com.hoanhoang.pages.*;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class TC02 extends BaseTest {

    @Test
    public void tc2_buyMultipleItemsSuccessfully(ITestContext context) {
        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        homePage.goToLoginPage();
        loginPage.login(getAssignedAccount().getUsername(), getAssignedAccount().getPassword());

        homePage.goToShopPage();

        List<Map<String, String>> products = shopPage.addMultipleRandomProductsToCart();

        homePage.goToCartPage();

        List<String> cartProductNames = shoppingCartPage.getAllProductNames();
        List<String> cartProductPrices = shoppingCartPage.getAllProductPrices();

        for (int i = 0; i < products.size(); i++) {
            String expectedName = products.get(i).get("name");
            String expectedPrice = products.get(i).get("price");
            String actualName = cartProductNames.get(i);
            String actualPrice = cartProductPrices.get(i);

            if (!actualName.equals(expectedName)) {
                throw new AssertionError("Product name at index " + i + " does not match. Expected: " + expectedName + ", Actual: " + actualName);
            }
            if (!actualPrice.equals(expectedPrice)) {
                throw new AssertionError("Product price at index " + i + " does not match. Expected: " + expectedPrice + ", Actual: " + actualPrice);
            }
        }

        shoppingCartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();
        checkoutPage.fillBillingDetails(billingInfo);
        checkoutPage.clickPlaceOrder();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();

        String statusMessage = orderSuccessPage.getStatusMessage();
        if (!statusMessage.contains("Thank you") && !statusMessage.contains("received")) {
            throw new AssertionError("Order confirmation message should show correctly");
        }

        orderSuccessPage.verifyBillingName(billingInfo.getFirstName(), billingInfo.getLastName());
    }
}
