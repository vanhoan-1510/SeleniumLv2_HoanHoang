package com.hoanhoang.tests;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.BrowserType;
import com.hoanhoang.core.driver.DriverFactory;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.OrderSuccessPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestContext;

public class BaseTest {

    @BeforeMethod
    public void setUp(ITestContext context) {
        String env = System.getProperty("env", "dev");
        ConfigLoader.load(env);
        String browser = ConfigLoader.getConfig().getBrowser();
        boolean headless = ConfigLoader.getConfig().isHeadless();
        BrowserType browserType = BrowserType.valueOf(browser.toUpperCase());
        DriverManager.setDriver(DriverFactory.createDriver(browserType, headless));
        DriverManager.getDriver().get(ConfigLoader.getConfig().getBaseUrl());
        context.setAttribute("account", getAssignedAccount());
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
        DriverManager.removeDriver();
    }

    protected TestDataManager.UserCredentials getAssignedAccount() {
        return com.hoanhoang.data.AccountPool.getAvailableAccount();
    }

    protected TestDataManager.UserCredentials getAssignedAccount(ITestContext context) {
        Object account = context.getAttribute("account");
        if (account instanceof TestDataManager.UserCredentials) {
            return (TestDataManager.UserCredentials) account;
        }
        return getAssignedAccount();
    }

    protected void login() {
        TestDataManager.UserCredentials account = getAssignedAccount();
        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        homePage.goToLoginPage();
        loginPage.login(account.getUsername(), account.getPassword());
    }

    protected void login(ITestContext context) {
        TestDataManager.UserCredentials account = getAssignedAccount(context);
        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        homePage.goToLoginPage();
        loginPage.login(account.getUsername(), account.getPassword());
    }

    protected void login(TestDataManager.UserCredentials account) {
        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        homePage.goToLoginPage();
        loginPage.login(account.getUsername(), account.getPassword());
    }

    protected void createOrder() {
        TestDataManager.UserCredentials user = TestDataManager.getValidCredentials();

        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        homePage.goToLoginPage();
        loginPage.login(user.getUsername(), user.getPassword());

        homePage.goToShopPage();
        shopPage.addMultipleRandomProductsToCart().get(0);

        homePage.goToCartPage();
        shoppingCartPage.clickCheckoutButton();

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();
        checkoutPage.fillBillingDetails(billingInfo);
        checkoutPage.selectPaymentMethod(PaymentMethod.COD);
        checkoutPage.clickPlaceOrder();

        orderSuccessPage.isOrderSuccessHeadingVisible();
    }
}
