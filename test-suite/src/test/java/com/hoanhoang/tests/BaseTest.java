package com.hoanhoang.tests;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.BrowserType;
import com.hoanhoang.core.driver.DriverFactory;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.data.AccountPool;
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
import org.testng.ITestResult;

public class BaseTest {

    protected TestDataManager.UserCredentials currentAccount;

    @BeforeMethod
    public void setUp(ITestResult result) {
        String env = System.getProperty("env", "dev");
        ConfigLoader.load(env);
        String browser = ConfigLoader.getConfig().getBrowser();
        boolean headless = ConfigLoader.getConfig().isHeadless();
        BrowserType browserType = BrowserType.valueOf(browser.toUpperCase());
        DriverManager.setDriver(DriverFactory.createDriver(browserType, headless));
        DriverManager.getDriver().get(ConfigLoader.getConfig().getBaseUrl());

        currentAccount = waitForAvailableAccount();
        if (currentAccount == null) {
            throw new IllegalStateException("No available account after timeout. All accounts are in use. Please reduce thread count or add more accounts.");
        }
        result.setAttribute("account", currentAccount);
    }

    protected TestDataManager.UserCredentials waitForAvailableAccount() {
        int timeoutSeconds = Integer.parseInt(System.getProperty("account.timeout", "60"));
        return AccountPool.waitForAvailableAccount(timeoutSeconds);
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
        DriverManager.removeDriver();
        if (currentAccount != null) {
            AccountPool.releaseAccount(currentAccount);
            currentAccount = null;
        }
    }

    protected TestDataManager.UserCredentials getAssignedAccount() {
        return AccountPool.getAvailableAccount();
    }

    protected TestDataManager.UserCredentials getAssignedAccount(ITestContext context) {
        if (currentAccount != null) {
            return currentAccount;
        }
        return getAssignedAccount();
    }

    protected void login() {
        if (currentAccount == null) {
            currentAccount = getAssignedAccount();
        }
        HomePage homePage = new HomePage();
        LoginPage loginPage = new LoginPage();
        homePage.goToLoginPage();
        loginPage.login(currentAccount.getUsername(), currentAccount.getPassword());
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

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
    }
}