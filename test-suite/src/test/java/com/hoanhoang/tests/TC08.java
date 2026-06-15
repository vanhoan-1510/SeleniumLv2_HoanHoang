package com.hoanhoang.tests;

import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC08 extends BaseTest {

    @Test
    public void userCanClearCart(ITestContext context) {
        HeaderComponent header = new HeaderComponent();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage cartPage = new ShoppingCartPage();

        login();

        header.goToCartPage();
        cartPage.removeAllProducts();

        header.goToShopPage();
        shopPage.addFirstProductToCart();

        header.goToCartPage();

        cartPage.verifyProductInCart();

        cartPage.clearCartUsingClearCartButton();

        cartPage.verifyCartCleared();
    }
}
