package com.hoanhoang.pages.components;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.OrderSummary;
import com.hoanhoang.data.util.PriceUtils;
import java.time.LocalDate;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OrderTableComponent {
    private static final By TABLE =
            By.cssSelector("table.account-orders-table, table.woocommerce-orders-table");
    private static final By ORDER_LINK = By.cssSelector("td[data-title='Order'] a");
    private final TableComponent<OrderSummary> table =
            new TableComponent<>(
                    TABLE,
                    By.cssSelector(
                            "table.account-orders-table tbody tr, table.woocommerce-orders-table tbody tr"),
                    OrderTableComponent::toOrder,
                    OrderTableComponent::orderNumber);

    private static String orderNumber(WebElement row) {
        return row.findElement(ORDER_LINK).getText().replace("#", "").trim();
    }

    private static OrderSummary toOrder(WebElement row) {
        return new OrderSummary(
                orderNumber(row),
                LocalDate.parse(
                        row.findElement(By.cssSelector("time"))
                                .getAttribute("datetime")
                                .substring(0, 10)),
                PriceUtils.parse(
                        row.findElement(By.cssSelector("td[data-title='Total'] .amount"))
                                .getText()),
                row.findElement(By.cssSelector("td[data-title='Status']")).getText().trim());
    }

    public boolean isVisible() {
        return table.isVisible();
    }

    public List<OrderSummary> getOrders() {
        UiWait.visible(TABLE);
        return table.getRows();
    }
}
