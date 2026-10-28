package com.hoanhoang.tests;

import static com.hoanhoang.tests.support.PurchaseAssertions.assertCart;
import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.model.Product;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import java.util.List;
import org.testng.annotations.Test;

public class TC08 extends BaseTest {

    @Test(description = "TC08 - Verify users can clear the cart")
    public void userCanClearCart() {
        new HomePage().open();
        login();
        openShopWithEmptyCart();

        HeaderComponent header = new HeaderComponent();
        Product expectedProduct = new ShopPage().addFirstProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        assertThat(cart.isProductTableVisible()).as("Cart table").isTrue();
        assertCart(cart.getProducts(), List.of(expectedProduct));

        cart.clearCartUsingClearCartButton();

        assertThat(cart.getProducts()).as("Clear shopping cart removes all rows").isEmpty();
        assertThat(cart.getEmptyCartMessage())
                .as("Empty-cart page message")
                .containsIgnoringCase("YOUR SHOPPING CART IS EMPTY");
    }
}
