package com.hoanhoang.tests;

import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import com.hoanhoang.pages.CheckoutPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC07 extends BaseTest {

    @Test
    public void properErrorHandlingWhenMandatoryFieldsBlank(ITestContext context) {
        HomePage homePage = new HomePage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage cartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();

        shopPage.goToShopPage();
        shopPage.addFirstProductToCart();

        homePage.goToCartPage();
        cartPage.clickCheckoutButton();

        checkoutPage.clickPlaceOrder();

        checkoutPage.verifyErrorMessageDisplayed();

        String errorText = checkoutPage.getErrorMessageText();
        if (errorText == null || errorText.isEmpty()) {
            throw new AssertionError("Error message should not be empty");
        }

        if (!checkoutPage.isFirstNameErrorVisible() && !checkoutPage.isLastNameErrorVisible() &&
            !checkoutPage.isAddressErrorVisible() && !checkoutPage.isCityErrorVisible() &&
            !checkoutPage.isPostcodeErrorVisible() && !checkoutPage.isPhoneErrorVisible()) {
            throw new AssertionError("At least one mandatory field error should be highlighted");
        }
    }
}
