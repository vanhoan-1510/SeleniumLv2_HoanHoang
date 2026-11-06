package com.hoanhoang.pages;

import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
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
    private final Element emptyMessage =
            $(By.cssSelector(".cart-empty, .empty-cart-block, .cart-empty-page"));
    private final Element checkoutButton = $(By.cssSelector("a.checkout-button"));
    private final Element clearCartButton = $(By.cssSelector(".clear-cart"));
    private final Elements headings = $$(By.tagName("h1"));

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

    @Step("Remove all products from cart")
    public void removeAllProducts() {
        waitUntilLoaded();
        table.removeAll();
    }

    @Step("Set quantity {quantity} for {name}, click UPDATE CART and wait for the response")
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
        checkoutButton.click();
    }

    @Step("Click 'Clear shopping cart' and confirm")
    public void clearCartUsingClearCartButton() {
        clearCartButton.click();
        UiWait.until("Clear-cart confirmation alert", ExpectedConditions.alertIsPresent()).accept();
        ApplicationWait.idle();
    }

    public boolean isEmptyCartMessageVisible() {
        return emptyMessage.isVisible()
                || headings.visible()
                        .readSnapshot(
                                visibleHeadings ->
                                        visibleHeadings.stream()
                                                .anyMatch(
                                                        e ->
                                                                e.getText()
                                                                        .toUpperCase(Locale.ROOT)
                                                                        .contains(
                                                                                "YOUR SHOPPING CART IS EMPTY")));
    }

    public String getEmptyCartMessage() {
        if (emptyMessage.isVisible()) {
            return emptyMessage.getText();
        }
        return headings.visible().map(Element::getText).stream()
                .filter(text -> text.toUpperCase(Locale.ROOT).contains("EMPTY"))
                .findFirst()
                .orElse("");
    }
}
