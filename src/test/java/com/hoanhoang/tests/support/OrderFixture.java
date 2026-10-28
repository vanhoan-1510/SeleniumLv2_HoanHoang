package com.hoanhoang.tests.support;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.OrderSuccessPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import java.util.ArrayList;
import java.util.List;

/** Explicit TC05 precondition. Uses UI because no seed API/DB is configured. */
public final class OrderFixture {
    private OrderFixture() {}

    public static List<String> createOrders(int count) {
        HeaderComponent header = new HeaderComponent();
        List<String> orderNumbers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orderNumbers.add(createOrder(header));
        }
        return List.copyOf(orderNumbers);
    }

    private static String createOrder(HeaderComponent header) {
        header.goToCartPage();
        new ShoppingCartPage().removeAllProducts();
        header.goToShopPage();
        new ShopPage().addFirstProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        cart.clickCheckoutButton();

        CheckoutPage checkout = new CheckoutPage();
        checkout.waitUntilLoaded();
        checkout.fillBillingDetails(TestDataManager.getBillingInfo());
        checkout.placeOrder();

        OrderSuccessPage success = new OrderSuccessPage();
        success.waitUntilLoaded();
        return success.getOrderNumber();
    }
}
