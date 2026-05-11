package com.hoanhoang.tests;

import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC09 extends BaseTest {

    @Test
    public void userCanUpdateQuantityInCart(ITestContext context) {
        HomePage homePage = new HomePage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage cartPage = new ShoppingCartPage();

        shopPage.goToShopPage();
        shopPage.addFirstProductToCart();

        homePage.goToCartPage();

        String initialQty = cartPage.getProductQuantityField();
        String initialSubTotal = cartPage.getSubTotalPrice();

        cartPage.clickQuantityPlusButton();

        String afterPlusQty = cartPage.getProductQuantityField();
        String afterPlusSubTotal = cartPage.getSubTotalPrice();

        if (afterPlusQty.equals(initialQty)) {
            throw new AssertionError("Quantity should increase after clicking plus button");
        }
        if (afterPlusSubTotal.equals(initialSubTotal)) {
            throw new AssertionError("Sub total should update after clicking plus button");
        }

        cartPage.setProductQuantity("4");
        cartPage.clickUpdateCartButton();

        cartPage.verifyProductQuantity("4");

        String updatedSubTotal = cartPage.getSubTotalPrice();

        if (updatedSubTotal.equals(afterPlusSubTotal)) {
            throw new AssertionError("Sub total should update after manual update");
        }

        cartPage.clickQuantityButton();

        String afterMinusQty = cartPage.getProductQuantityField();
        String afterMinusSubTotal = cartPage.getSubTotalPrice();

        if (Integer.parseInt(afterMinusQty) >= 4) {
            throw new AssertionError("Quantity should decrease after clicking minus button");
        }
        if (afterMinusSubTotal.equals(updatedSubTotal)) {
            throw new AssertionError("Sub total should update after clicking minus button");
        }
    }
}
