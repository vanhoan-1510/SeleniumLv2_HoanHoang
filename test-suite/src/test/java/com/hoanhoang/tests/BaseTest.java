package com.hoanhoang.tests;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.BrowserType;
import com.hoanhoang.core.driver.DriverFactory;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.RetryHandler;
import com.hoanhoang.data.AccountPool;
import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.OrderSuccessPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.openqa.selenium.JavascriptExecutor;

public class BaseTest {

    protected TestDataManager.UserCredentials currentAccount;

    @BeforeMethod
    public void setUp(ITestResult result) {
        String env = System.getProperty("env", "dev");
        ConfigLoader.load(env);
        String browser = System.getProperty("browser", ConfigLoader.getConfig().getBrowser());
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", String.valueOf(ConfigLoader.getConfig().isHeadless()))
        );
        BrowserType browserType = BrowserType.valueOf(browser.toUpperCase());
        DriverManager.setDriver(DriverFactory.createDriver(browserType, headless));
        openBaseUrl();

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

    private void openBaseUrl() {
        RetryHandler.retryUntilTimeout(
                () -> DriverManager.getDriver().get(ConfigLoader.getConfig().getBaseUrl()),
                getSetupRetryTimeoutMs(),
                ConfigLoader.getConfig().getStepRetryIntervalMs(),
                this::stopCurrentPageLoad
        );
    }

    private long getSetupRetryTimeoutMs() {
        return Math.max(
                ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(),
                ConfigLoader.getConfig().getTestRetryTimeoutMs()
        );
    }

    private void stopCurrentPageLoad() {
        try {
            ((JavascriptExecutor) DriverManager.getDriver()).executeScript("window.stop();");
        } catch (RuntimeException ignored) {
            // Browser may already be between navigations; the next retry will drive it again.
        }
    }

    protected TestDataManager.UserCredentials getAssignedAccount() {
        if (currentAccount != null) {
            return currentAccount;
        }
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
        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        header.goToLoginPage();
        loginPage.login(currentAccount.getUsername(), currentAccount.getPassword());
    }

    protected void login(ITestContext context) {
        TestDataManager.UserCredentials account = getAssignedAccount(context);
        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        header.goToLoginPage();
        loginPage.login(account.getUsername(), account.getPassword());
    }

    protected void login(TestDataManager.UserCredentials account) {
        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        header.goToLoginPage();
        loginPage.login(account.getUsername(), account.getPassword());
    }

    protected void createOrder() {
        TestDataManager.UserCredentials user = getAssignedAccount();

        HeaderComponent header = new HeaderComponent();
        LoginPage loginPage = new LoginPage();
        ShopPage shopPage = new ShopPage();
        ShoppingCartPage shoppingCartPage = new ShoppingCartPage();
        CheckoutPage checkoutPage = new CheckoutPage();
        OrderSuccessPage orderSuccessPage = new OrderSuccessPage();

        header.goToLoginPage();
        if (loginPage.isLoginFormVisible()) {
            loginPage.login(user.getUsername(), user.getPassword());
        }

        header.goToCartPage();
        shoppingCartPage.removeAllProducts();

        header.goToShopPage();
        shopPage.addRandomProductToCart();

        header.goToCartPage();
        shoppingCartPage.clickCheckoutButton();

        TestDataManager.BillingDetails billingInfo = TestDataManager.getBillingInfo();
        checkoutPage.fillBillingDetails(billingInfo);
        checkoutPage.selectPaymentMethod(PaymentMethod.COD);
        checkoutPage.clickPlaceOrderExpectingSuccess();

        orderSuccessPage.verifyOrderSuccessHeadingVisible();
    }
}
