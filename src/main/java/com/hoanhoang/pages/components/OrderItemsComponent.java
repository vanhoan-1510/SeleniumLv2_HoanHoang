package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$$;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.data.util.PriceUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.By;

/** Row-scoped reader shared by checkout and order confirmation tables. */
public class OrderItemsComponent {
    private static final Pattern QUANTITY = Pattern.compile("[×x]\\s*(\\d+)\\s*$");
    private static final By PRODUCT_NAME = By.cssSelector("td.product-name");
    private static final By PRODUCT_LINK = By.cssSelector("a");
    private static final By PRODUCT_TOTAL = By.cssSelector("td.product-total .amount");
    private final Elements rows;

    public OrderItemsComponent(By rows) {
        this.rows = $$(rows).visible();
    }

    public List<OrderItem> getItems() {
        return rows.waitUntil(
                "Order item rows loaded",
                visible -> {
                    List<OrderItem> items =
                            visible.stream().map(OrderItemsComponent::readItem).toList();
                    return items.isEmpty() ? null : items;
                });
    }

    private static OrderItem readItem(Element row) {
        Element nameCell = row.find(PRODUCT_NAME);
        String text = nameCell.getText();
        var quantityMatcher = QUANTITY.matcher(text);
        int quantity = 1;
        String name = text;
        if (quantityMatcher.find()) {
            quantity = Integer.parseInt(quantityMatcher.group(1));
            name = text.substring(0, quantityMatcher.start()).trim();
        }

        Elements links = nameCell.findAll(PRODUCT_LINK);
        if (!links.isEmpty()) {
            name = links.first().getText();
        }
        return new OrderItem(name, quantity, PriceUtils.parse(row.find(PRODUCT_TOTAL).getText()));
    }
}
