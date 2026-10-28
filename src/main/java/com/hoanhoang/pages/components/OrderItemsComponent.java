package com.hoanhoang.pages.components;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.data.util.PriceUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** Row-scoped reader shared by checkout and order confirmation tables. */
public class OrderItemsComponent {
    private final By rows;
    private static final Pattern QUANTITY = Pattern.compile("[×x]\\s*(\\d+)\\s*$");

    public OrderItemsComponent(By rows) {
        this.rows = rows;
    }

    public List<OrderItem> getItems() {
        return UiWait.until(
                "Order item rows loaded",
                d -> {
                    List<WebElement> elements = UiWait.visibleElements(rows);
                    if (elements.isEmpty()) {
                        return null;
                    }
                    return elements.stream().map(OrderItemsComponent::readItem).toList();
                });
    }

    private static OrderItem readItem(WebElement row) {
        WebElement nameCell = row.findElement(By.cssSelector("td.product-name"));
        String text = nameCell.getText().trim();
        var quantityMatcher = QUANTITY.matcher(text);
        int quantity = 1;
        String name = text;
        if (quantityMatcher.find()) {
            quantity = Integer.parseInt(quantityMatcher.group(1));
            name = text.substring(0, quantityMatcher.start()).trim();
        }

        List<WebElement> links = nameCell.findElements(By.cssSelector("a"));
        if (!links.isEmpty()) {
            name = links.get(0).getText().trim();
        }
        return new OrderItem(
                name,
                quantity,
                PriceUtils.parse(
                        row.findElement(By.cssSelector("td.product-total .amount")).getText()));
    }
}
