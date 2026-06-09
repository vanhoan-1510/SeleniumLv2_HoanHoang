package com.hoanhoang.tests;

import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import com.hoanhoang.pages.CheckoutPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC07 extends BaseTest {

    @Test
    public void properErrorHandlingWhenMandatoryFieldsBlank(ITestContext context) {
        HeaderComponent header = new HeaderComponent();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage cartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();

        header.goToShopPage();
        shopPage.addFirstProductToCart();

        header.goToCartPage();
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
