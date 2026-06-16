package com.hoanhoang.tests;

import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.*;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC06 extends BaseTest {

    @Test
    public void guestCanPurchaseOrderSuccessfully(ITestContext context) {
        HeaderComponent header = new HeaderComponent();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage cartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        header.goToShopPage();

        shopPage.verifyGridViewVisible();

        shopPage.switchToListView();
        shopPage.verifyListViewVisible();

        shopPage.addFirstProductToCart();

        header.goToCartPage();

        String productName = cartPage.getProductName();
        String productPrice = cartPage.getProductPrice();

        cartPage.clickCheckoutButton();

        checkoutPage.verifyCheckoutHeadingVisible();

        checkoutPage.fillBillingInfo("John", "Doe", "123 Main St", "New York", "10001", "1234567890");
        checkoutPage.selectPaymentMethod(PaymentMethod.COD);
        checkoutPage.clickPlaceOrderExpectingSuccess();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
        orderSuccessPage.verifyOrderConfirmationMessage();
        orderSuccessPage.verifyOrderedProductName(productName);
        orderSuccessPage.verifyOrderedProductPrice(productPrice);
    }
}
