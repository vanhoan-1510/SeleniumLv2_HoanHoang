package com.hoanhoang.tests;

import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.ShopPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC04 extends BaseTest {

    @Test
    public void tc4_sortItemsByPrice(ITestContext context) {
        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();

        homePage.goToLoginPage();
        loginPage.login(getAssignedAccount().getUsername(), getAssignedAccount().getPassword());

        homePage.goToShopPage();

        shopPage.sortByPriceLowToHigh();
        shopPage.verifyPricesSortedLowToHigh();

        shopPage.sortByPriceHighToLow();
        shopPage.verifyPricesSortedHighToLow();
    }
}
