package com.hoanhoang.tests;

import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.ShopPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC04 extends BaseTest {

    @Test
    public void tc4_sortItemsByPrice(ITestContext context) {
        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();

        header.goToLoginPage();
        loginPage.login(getAssignedAccount().getUsername(), getAssignedAccount().getPassword());

        header.goToShopPage();

        shopPage.sortByPriceLowToHigh();
        shopPage.verifyPricesSortedLowToHigh();

        shopPage.sortByPriceHighToLow();
        shopPage.verifyPricesSortedHighToLow();
    }
}
