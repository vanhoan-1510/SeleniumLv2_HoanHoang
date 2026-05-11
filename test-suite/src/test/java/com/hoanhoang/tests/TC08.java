package com.hoanhoang.tests;

import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC08 extends BaseTest {

    @Test
    public void userCanClearCart(ITestContext context) {
        HomePage homePage = new HomePage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage cartPage = new ShoppingCartPage();

        shopPage.goToShopPage();
        shopPage.addFirstProductToCart();

        homePage.goToCartPage();

        cartPage.verifyProductInCart();

        cartPage.clearCart();

        cartPage.verifyCartCleared();
    }
}
