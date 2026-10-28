package com.hoanhoang.pages;

import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.CartItem;
import com.hoanhoang.pages.components.CartTableComponent;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;
import java.util.List;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ShoppingCartPage extends BasePage {
    private final CartTableComponent table = new CartTableComponent();
    private final By emptyMessage =
            By.cssSelector(".cart-empty, .empty-cart-block, .cart-empty-page");

    public void waitUntilLoaded() {
        ApplicationWait.idle();
        UiWait.until(
                "Cart table or empty-cart state loaded",
                d -> table.isVisible() || isEmptyCartMessageVisible());
    }

    @Step("Reload Shopping Cart page")
    public void reloadAndWaitUntilReady() {
        DriverManager.getDriver().navigate().refresh();
        waitUntilLoaded();
    }

    public List<CartItem> getProducts() {
        return table.getItems();
    }

    public CartItem getProduct(String name) {
        return table.getItem(name);
    }

    public boolean isProductTableVisible() {
        return table.isVisible();
    }

    @Step("Remove {name} from cart")
    public void removeProduct(String name) {
        waitUntilLoaded();
        table.removeProduct(name);
    }

    @Step("Remove all products from cart")
    public void removeAllProducts() {
        waitUntilLoaded();
        table.removeAll();
    }

    @Step("Set quantity {quantity} for {name} and wait for the cart update")
    public void setQuantity(String name, int quantity) {
        table.setQuantity(name, quantity);
    }

    @Step("Click Plus (+) for {name}")
    public void increaseQuantity(String name) {
        table.changeQuantity(name, 1);
    }

    @Step("Click Minus (-) for {name}")
    public void decreaseQuantity(String name) {
        table.changeQuantity(name, -1);
    }

    @Step("Click 'Proceed to checkout'")
    public void clickCheckoutButton() {
        ApplicationWait.idle();
        UiWait.click(By.cssSelector("a.checkout-button"));
    }

    @Step("Click 'Clear shopping cart' and confirm")
    public void clearCartUsingClearCartButton() {
        UiWait.click(By.cssSelector(".clear-cart"));
        UiWait.until("Clear-cart confirmation alert", ExpectedConditions.alertIsPresent()).accept();
        ApplicationWait.idle();
    }

    public boolean isEmptyCartMessageVisible() {
        return UiWait.isVisible(emptyMessage)
                || DriverManager.getDriver().findElements(By.tagName("h1")).stream()
                        .anyMatch(
                                e ->
                                        e.isDisplayed()
                                                && e.getText()
                                                        .toUpperCase(Locale.ROOT)
                                                        .contains("YOUR SHOPPING CART IS EMPTY"));
    }

    public String getEmptyCartMessage() {
        if (UiWait.isVisible(emptyMessage)) {
            return UiWait.visible(emptyMessage).getText().trim();
        }
        return DriverManager.getDriver().findElements(By.tagName("h1")).stream()
                .filter(
                        e ->
                                e.isDisplayed()
                                        && e.getText().toUpperCase(Locale.ROOT).contains("EMPTY"))
                .map(e -> e.getText().trim())
                .findFirst()
                .orElse("");
    }
}
