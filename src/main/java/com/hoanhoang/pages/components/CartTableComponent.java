package com.hoanhoang.pages.components;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.CartItem;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import java.math.BigDecimal;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class CartTableComponent {
    private static final By ROWS = By.cssSelector("table.shop_table.cart tbody tr.cart_item");
    private static final By NAME = By.cssSelector(".product-title");
    private static final By QUANTITY = By.cssSelector(".product-quantity input.qty");
    private static final By REMOVE = By.cssSelector("a.remove-item");
    private final TableComponent<CartItem> table =
            new TableComponent<>(
                    By.cssSelector("table.shop_table.cart"),
                    ROWS,
                    CartTableComponent::toItem,
                    row -> row.findElement(NAME).getText().trim());

    private static CartItem toItem(WebElement row) {
        return new CartItem(
                row.findElement(NAME).getText().trim(),
                PriceUtils.parse(
                        row.findElement(By.cssSelector(".product-price .amount")).getText()),
                Integer.parseInt(row.findElement(QUANTITY).getDomProperty("value")),
                PriceUtils.parse(
                        row.findElement(By.cssSelector(".product-subtotal .amount")).getText()));
    }

    public boolean isVisible() {
        return table.isVisible();
    }

    public List<CartItem> getItems() {
        return table.getRows();
    }

    public CartItem getItem(String name) {
        return table.get(name);
    }

    public void setQuantity(String name, int quantity) {
        validateQuantity(quantity);
        ApplicationWait.idle();
        if (getItem(name).quantity() == quantity) {
            waitForQuantity(name, quantity);
            ApplicationWait.idle();
            return;
        }
        WebElement input = table.row(name).findElement(QUANTITY);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(String.valueOf(quantity));
        // Blur triggers the theme's debounced change handler (1500ms). A manual UPDATE CART
        // click here would submit once now and again when that timer fires.
        input.sendKeys(Keys.TAB);
        waitForQuantity(name, quantity);
        ApplicationWait.idle();
    }

    public void changeQuantity(String name, int delta) {
        if (delta != -1 && delta != 1) {
            throw new IllegalArgumentException("Quantity delta must be -1 or 1");
        }
        ApplicationWait.idle();
        int expectedQuantity = getItem(name).quantity() + delta;
        validateQuantity(expectedQuantity);
        table.clickInRow(name, By.cssSelector(delta > 0 ? ".plus" : ".minus"));
        // The theme submits the cart by itself 1.5s after +/- (debounced AJAX). Do not click
        // UPDATE CART here: a second update overlaps it and the stale response wins. The subtotal
        // only changes when the server has answered, so waiting for it means the update is done.
        waitForQuantity(name, expectedQuantity);
        ApplicationWait.idle();
    }

    private void waitForQuantity(String name, int expectedQuantity) {
        CartItem[] lastItem = {null};
        IllegalArgumentException[] lastReadError = {null};
        try {
            UiWait.until(
                    "Cart quantity and totals settled for " + name,
                    d -> {
                        WebElement row = findVisibleRow(name);
                        if (row == null) {
                            lastItem[0] = null;
                            return false;
                        }
                        try {
                            lastItem[0] = toItem(row);
                        } catch (IllegalArgumentException e) {
                            // Quantity/price cells can be blank while the row is being replaced.
                            lastReadError[0] = e;
                            return false;
                        }
                        return quantityMatchesServerState(row, lastItem[0], expectedQuantity);
                    });
        } catch (TimeoutException e) {
            if (lastReadError[0] != null) {
                e.addSuppressed(lastReadError[0]);
            }
            throw new TimeoutException(
                    "Expected cart quantity "
                            + expectedQuantity
                            + " for "
                            + name
                            + "; last row: "
                            + lastItem[0],
                    e);
        }
    }

    private static WebElement findVisibleRow(String name) {
        return UiWait.visibleElements(ROWS).stream()
                .filter(row -> name.equals(row.findElement(NAME).getText().trim()))
                .findFirst()
                .orElse(null);
    }

    private static boolean quantityMatchesServerState(
            WebElement row, CartItem item, int expectedQuantity) {
        return String.valueOf(expectedQuantity)
                        .equals(row.findElement(QUANTITY).getDomAttribute("value"))
                && item.quantity() == expectedQuantity
                && item.subtotal()
                                .compareTo(
                                        item.unitPrice()
                                                .multiply(BigDecimal.valueOf(expectedQuantity)))
                        == 0;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1; use removeProduct to remove an item");
        }
    }

    public void removeProduct(String name) {
        int count = table.size();
        table.clickInRow(name, REMOVE);
        UiWait.until("Cart row removed: " + name, d -> d.findElements(ROWS).size() < count);
        ApplicationWait.idle();
    }

    public void removeAll() {
        while (!getItems().isEmpty()) {
            int count = table.size();
            table.firstRow().findElement(REMOVE).click();
            UiWait.until("Cart row removed", d -> d.findElements(ROWS).size() < count);
            ApplicationWait.idle();
        }
    }
}
