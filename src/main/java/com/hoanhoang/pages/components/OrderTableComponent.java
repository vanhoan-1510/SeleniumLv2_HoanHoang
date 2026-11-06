package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;
import static com.hoanhoang.pages.components.TableComponent.requiredCellValue;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.data.model.OrderSummary;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import java.time.LocalDate;
import java.util.List;
import org.openqa.selenium.By;

public class OrderTableComponent {
    private static final By TABLE =
            By.cssSelector("table.account-orders-table, table.woocommerce-orders-table");
    private static final By ORDER_LINK = By.cssSelector("td[data-title='Order'] a");
    private static final By ROWS =
            By.cssSelector(
                    "table.account-orders-table tbody tr, table.woocommerce-orders-table tbody tr");
    private static final By DATE = By.cssSelector("time");
    private static final By TOTAL = By.cssSelector("td[data-title='Total'] .amount");
    private static final By STATUS = By.cssSelector("td[data-title='Status']");
    private final TableComponent<OrderSummary> table =
            new TableComponent<>(
                    TABLE, ROWS, OrderTableComponent::toOrder, OrderTableComponent::orderNumber);

    private static String orderNumber(Element row) {
        return row.find(ORDER_LINK).getText().replace("#", "").trim();
    }

    private static OrderSummary toOrder(Element row) {
        return new OrderSummary(
                orderNumber(row),
                LocalDate.parse(
                        requiredCellValue(row.find(DATE).getDomAttribute("datetime"), "order date")
                                .substring(0, 10)),
                PriceUtils.parse(requiredCellValue(row.find(TOTAL).getText(), "order total")),
                row.find(STATUS).getText());
    }

    public List<OrderSummary> getOrders() {
        $(TABLE).waitUntil(
                        "Order history table displayed",
                        history -> {
                            ApplicationWait.failIfCriticalError();
                            return history.isVisible();
                        });
        return table.getRows();
    }
}
