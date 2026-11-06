package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;
import static com.hoanhoang.core.element.ElementFactory.$$;
import static com.hoanhoang.pages.components.TableComponent.requiredCellValue;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.data.model.CartItem;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.openqa.selenium.By;

public class CartTableComponent {
    private static final By TABLE = By.cssSelector("table.shop_table.cart");
    private static final By ROWS = By.cssSelector("table.shop_table.cart tbody tr.cart_item");
    private static final By NAME = By.cssSelector(".product-title");
    private static final By QUANTITY = By.cssSelector(".product-quantity input.qty");
    private static final By REMOVE = By.cssSelector("a.remove-item");
    private static final By UNIT_PRICE = By.cssSelector(".product-price .amount");
    private static final By SUBTOTAL = By.cssSelector(".product-subtotal .amount");
    private static final By INCREASE_QUANTITY = By.cssSelector(".plus");
    private static final By DECREASE_QUANTITY = By.cssSelector(".minus");
    private final Element updateCartButton = $(By.cssSelector("button[name='update_cart']"));
    private final Elements rows = $$(ROWS).visible();
    private final TableComponent<CartItem> table =
            new TableComponent<>(
                    TABLE, ROWS, CartTableComponent::toItem, row -> row.find(NAME).getText());

    private static CartItem toItem(Element row) {
        return new CartItem(
                row.find(NAME).getText(),
                PriceUtils.parse(requiredCellValue(row.find(UNIT_PRICE).getText(), "unit price")),
                Integer.parseInt(
                        requiredCellValue(row.find(QUANTITY).getDomProperty("value"), "quantity")),
                PriceUtils.parse(requiredCellValue(row.find(SUBTOTAL).getText(), "subtotal")));
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
            return;
        }
        Element row = table.row(name);
        BooleanSupplier rowReplaced = row.watchForReplacement();
        row.find(QUANTITY).replaceTextAndBlur(String.valueOf(quantity));
        updateCartButton.click();
        waitForUpdate(name, rowReplaced);
    }

    public void changeQuantity(String name, int delta) {
        if (delta != -1 && delta != 1) {
            throw new IllegalArgumentException("Quantity delta must be -1 or 1");
        }
        ApplicationWait.idle();
        int expectedQuantity = getItem(name).quantity() + delta;
        validateQuantity(expectedQuantity);
        BooleanSupplier rowReplaced = table.row(name).watchForReplacement();
        table.clickInRow(name, delta > 0 ? INCREASE_QUANTITY : DECREASE_QUANTITY);
        // The theme submits the cart by itself 1.5s after +/- (debounced AJAX). Do not click
        // UPDATE CART here: a second update can overlap it. Wait for the response to replace the
        // original row; quantity and subtotal are verified by the test after the update finishes.
        waitForUpdate(name, rowReplaced);
    }

    private void waitForUpdate(String name, BooleanSupplier rowReplaced) {
        rows.waitUntil("Cart response received for " + name, visible -> rowReplaced.getAsBoolean());
        ApplicationWait.idle();
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
    }

    public void removeAll() {
        while (!getItems().isEmpty()) {
            int count = table.size();
            table.firstRow().find(REMOVE).click();
            rows.waitUntilSizeLessThan(count);
            ApplicationWait.idle();
        }
    }
}
