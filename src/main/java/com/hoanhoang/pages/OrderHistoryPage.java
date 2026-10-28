package com.hoanhoang.pages;

import com.hoanhoang.data.model.OrderSummary;
import com.hoanhoang.pages.components.OrderTableComponent;
import java.util.List;

public class OrderHistoryPage extends BasePage {
    private final OrderTableComponent table = new OrderTableComponent();

    public boolean isOrderTableVisible() {
        return table.isVisible();
    }

    public List<OrderSummary> getOrders() {
        return table.getOrders();
    }
}
