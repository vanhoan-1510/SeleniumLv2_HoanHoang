package com.hoanhoang.tests;

import static com.hoanhoang.tests.support.PurchaseAssertions.assertCart;
import static com.hoanhoang.tests.support.PurchaseAssertions.assertOrderItems;
import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.OrderSuccessPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TC03 extends BaseTest {

    @Test(
            dataProvider = "paymentMethods",
            description = "TC03 - Verify users can buy an item using different payment methods")
    public void userCanBuyAnItemWithEachPaymentMethod(PaymentMethod paymentMethod) {
        new HomePage().open();
        login();
        openShopWithEmptyCart();

        HeaderComponent header = new HeaderComponent();
        Product expectedProduct = new ShopPage().addRandomProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        assertCart(cart.getProducts(), List.of(expectedProduct));

        cart.clickCheckoutButton();
        CheckoutPage checkout = new CheckoutPage();
        checkout.waitUntilLoaded();

        checkout.fillBillingDetails(TestDataManager.getBillingInfo());
        checkout.selectPaymentMethod(paymentMethod);
        checkout.placeOrder();

        OrderSuccessPage success = new OrderSuccessPage();
        success.waitUntilLoaded();
        success.confirmationMessage().shouldHaveText("Thank you");
        assertThat(success.getPaymentMethod())
                .as("Selected payment method")
                .isEqualToIgnoringCase(paymentMethod.getDisplayName());
        assertOrderItems(success.getProducts(), List.of(expectedProduct));
    }

    /** One run per payment method listed in testdata/payment-methods.json. */
    @DataProvider(name = "paymentMethods")
    public Object[][] paymentMethods() {
        return TestDataManager.getPaymentMethods().stream()
                .map(paymentMethod -> new Object[] {paymentMethod})
                .toArray(Object[][]::new);
    }
}
