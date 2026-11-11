package com.hoanhoang.tests;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.model.UserDto;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.ShoppingCartPage;
import com.hoanhoang.tests.support.TestSession;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {
    private final TestSession session = new TestSession();

    @BeforeSuite(alwaysRun = true)
    public void loadConfiguration() {
        ConfigLoader.load(System.getProperty("env", "dev"));
        TestDataManager.requireCredentials();
    }

    /**
     * Infrastructure only: a fresh browser per test. Scenario steps (incl. opening the site) live
     * in the tests.
     */
    @BeforeMethod
    public void setUp() {
        DriverManager.startDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        session.close();
    }

    protected UserDto getAssignedAccount() {
        return session.account();
    }

    protected void login() {
        var account = getAssignedAccount();
        new HeaderComponent().goToLoginPage();
        new LoginPage().login(account.getUsername(), account.getPassword());
    }

    @Step("Open Shop with an empty cart")
    protected void openShopWithEmptyCart() {
        HeaderComponent header = new HeaderComponent();
        header.goToCartPage();
        new ShoppingCartPage().removeAllProducts();
        header.goToShopPage();
    }
}
