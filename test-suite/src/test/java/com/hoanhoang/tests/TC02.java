package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.*;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class TC02 extends BaseTest {

    @Test
    public void tc2_buyMultipleItemsSuccessfully(ITestContext context) {
        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        header.goToLoginPage();
        loginPage.login(getAssignedAccount().getUsername(), getAssignedAccount().getPassword());

        header.goToCartPage();
        shoppingCartPage.removeAllProducts();

        header.goToShopPage();

        List<Map<String, String>> products = shopPage.addMultipleRandomProductsToCart();

        header.goToCartPage();

        List<String> cartProductNames = shoppingCartPage.getAllProductNames();
        List<String> cartProductPrices = shoppingCartPage.getAllProductPrices();

        for (Map<String, String> product : products) {
            String expectedName = product.get("name");
            String expectedPrice = normalizePrice(product.get("price"));

            if (!cartProductNames.contains(expectedName)) {
                throw new AssertionError("Cart should contain product name: " + expectedName + ". Actual: " + cartProductNames);
            }
            if (!cartProductPrices.contains(expectedPrice)) {
                throw new AssertionError("Cart should contain product price: " + expectedPrice + ". Actual: " + cartProductPrices);
            }
        }

        shoppingCartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();
        checkoutPage.fillBillingDetails(billingInfo);
        checkoutPage.selectPaymentMethod(PaymentMethod.COD);
        checkoutPage.clickPlaceOrder();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();

        orderSuccessPage.verifyOrderConfirmationMessage();

        orderSuccessPage.verifyBillingName(billingInfo.getFirstName(), billingInfo.getLastName());
    }

    private String normalizePrice(String priceText) {
        String[] prices = priceText.trim().split("\\$");
        return "$" + prices[prices.length - 1].trim();
    }
}
