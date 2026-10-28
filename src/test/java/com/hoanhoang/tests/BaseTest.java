package com.hoanhoang.tests;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.data.AccountPool;
import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.model.UserDto;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.ShoppingCartPage;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {
    private final ThreadLocal<UserDto> accounts = new ThreadLocal<>();

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
        try {
            DriverManager.quitDriver();
        } finally {
            try {
                var account = accounts.get();
                if (account != null) {
                    AccountPool.releaseAccount(account);
                }
            } finally {
                accounts.remove();
            }
        }
    }

    protected UserDto getAssignedAccount() {
        var account = accounts.get();
        if (account == null) {
            int seconds = Integer.parseInt(System.getProperty("account.timeout", "60"));
            account = AccountPool.waitForAvailableAccount(seconds);
            if (account == null) {
                throw new IllegalStateException(
                        "No account available after " + seconds + " seconds");
            }
            accounts.set(account);
        }
        return account;
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
