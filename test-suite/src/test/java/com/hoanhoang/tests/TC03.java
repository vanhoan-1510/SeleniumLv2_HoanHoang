package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class TC03 extends BaseTest {

    @DataProvider(name = "paymentMethods")
    public Object[][] paymentMethods() {
        return new Object[][]{
                {PaymentMethod.BANK_TRANSFER},
                {PaymentMethod.CHEQUE},
                {PaymentMethod.COD}
        };
    }

    @Test(dataProvider = "paymentMethods")
    public void tc3_buyItemWithDifferentPaymentMethods(PaymentMethod paymentMethod) {
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

        Map<String, String> product = shopPage.addRandomProductToCart();

        header.goToCartPage();
        shoppingCartPage.verifyProductName(product.get("name"));
        shoppingCartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();

        checkoutPage.fillBillingDetails(TestDataManager.getBillingInfo());
        checkoutPage.selectPaymentMethod(paymentMethod);

        checkoutPage.clickPlaceOrderExpectingSuccess();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
        orderSuccessPage.verifyOrderConfirmationMessage();

        orderSuccessPage.verifyOrderedProductName(product.get("name"));

        String paymentMethodText = orderSuccessPage.getPaymentMethod();
        if (paymentMethodText == null) {
            throw new AssertionError("Payment method should be displayed");
        }
    }
}
