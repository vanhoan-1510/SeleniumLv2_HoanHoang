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

public class TC06 extends BaseTest {

    @Test(description = "TC06 - Verify users can buy an item without logging in (as a guest)")
    public void guestCanBuyAnItem() {
        new HomePage().open();

        HeaderComponent header = new HeaderComponent();
        header.goToShopPage();
        ShopPage shop = new ShopPage();
        shop.waitUntilLoaded();
        assertThat(shop.isGridViewVisible()).as("Default grid view").isTrue();

        shop.switchToListView();
        assertThat(shop.isListViewVisible()).as("List view").isTrue();

        Product expectedProduct = shop.addFirstProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        assertCart(cart.getProducts(), List.of(expectedProduct));

        cart.clickCheckoutButton();
        CheckoutPage checkout = new CheckoutPage();
        checkout.waitUntilLoaded();

        checkout.fillBillingDetails(TestDataManager.getBillingInfo());
        checkout.placeOrder();

        OrderSuccessPage success = new OrderSuccessPage();
        success.waitUntilLoaded();
        success.confirmationMessage().shouldHaveText("Thank you");
        assertOrderItems(success.getProducts(), List.of(expectedProduct));
    }
}
