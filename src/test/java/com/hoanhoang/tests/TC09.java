package com.hoanhoang.tests;

import static com.hoanhoang.tests.support.PurchaseAssertions.assertCart;
import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.model.CartItem;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.ShoppingCartPage;
import com.hoanhoang.tests.support.CartUpdateTrace;
import java.math.BigDecimal;
import java.util.List;
import org.testng.annotations.Test;

public class TC09 extends BaseTest {

    @Test(description = "TC09 - Verify users can update quantity of product in cart")
    public void userCanUpdateQuantityInCart() {
        new HomePage().open();
        login();
        openShopWithEmptyCart();

        HeaderComponent header = new HeaderComponent();
        Product expectedProduct = new ShopPage().addFirstProductToCart();

        header.goToCartPage();
        ShoppingCartPage cart = new ShoppingCartPage();
        cart.reloadAndWaitUntilReady();
        assertCart(cart.getProducts(), List.of(expectedProduct));

        try (var trace = new CartUpdateTrace()) {
            cart.increaseQuantity(expectedProduct.name());
            assertQuantity(cart.getProduct(expectedProduct.name()), expectedProduct, 2);

            cart.setQuantity(expectedProduct.name(), 4);
            assertQuantity(cart.getProduct(expectedProduct.name()), expectedProduct, 4);

            cart.decreaseQuantity(expectedProduct.name());
            assertQuantity(cart.getProduct(expectedProduct.name()), expectedProduct, 3);
        }

        cart.reloadAndWaitUntilReady();
        assertQuantity(cart.getProduct(expectedProduct.name()), expectedProduct, 3);
    }

    private void assertQuantity(
            CartItem actualItem, Product expectedProduct, int expectedQuantity) {
        assertThat(actualItem.quantity())
                .as("%s quantity", expectedProduct.name())
                .isEqualTo(expectedQuantity);
        assertThat(actualItem.subtotal())
                .as("%s subtotal at quantity %s", expectedProduct.name(), expectedQuantity)
                .isEqualByComparingTo(
                        expectedProduct.price().multiply(BigDecimal.valueOf(expectedQuantity)));
    }
}
