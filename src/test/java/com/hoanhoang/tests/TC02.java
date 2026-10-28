package com.hoanhoang.tests;

import static com.hoanhoang.tests.support.PurchaseAssertions.assertCart;
import static com.hoanhoang.tests.support.PurchaseAssertions.assertOrderItems;
import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.OrderSuccessPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import java.util.List;
import org.testng.annotations.Test;

public class TC02 extends BaseTest {

    @Test(description = "TC02 - Verify users can buy multiple items successfully")
    public void userCanBuyMultipleItems() {
        new HomePage().open();
        login();
        openShopWithEmptyCart();

        HeaderComponent header = new HeaderComponent();
        List<Product> expectedProducts = new ShopPage().addMultipleRandomProductsToCart();
        assertThat(expectedProducts)
                .as("Multiple products selected")
                .hasSizeGreaterThanOrEqualTo(2);

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        assertCart(cart.getProducts(), expectedProducts);

        cart.clickCheckoutButton();
        CheckoutPage checkout = new CheckoutPage();
        checkout.waitUntilLoaded();
        assertOrderItems(checkout.getProducts(), expectedProducts);

        checkout.fillBillingDetails(TestDataManager.getBillingInfo());
        checkout.placeOrder();

        OrderSuccessPage success = new OrderSuccessPage();
        success.waitUntilLoaded();
        success.confirmationMessage().shouldHaveText("Thank you");
        assertOrderItems(success.getProducts(), expectedProducts);
    }
}
