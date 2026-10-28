package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.model.OrderSummary;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.MyAccountPage;
import com.hoanhoang.pages.OrderHistoryPage;
import com.hoanhoang.tests.support.OrderFixture;
import java.util.List;
import org.testng.annotations.Test;

public class TC05 extends BaseTest {

    @Test(description = "TC05 - Verify orders appear in order history")
    public void placedOrdersAppearInOrderHistory() {
        new HomePage().open();
        login();
        List<String> createdOrderNumbers = OrderFixture.createOrders(2);

        new HeaderComponent().goToMyAccountPage();
        new MyAccountPage().clickOnOrders();
        OrderHistoryPage history = new OrderHistoryPage();
        List<OrderSummary> orders = history.getOrders();

        assertThat(history.isOrderTableVisible()).as("Order history table visible").isTrue();
        assertThat(orders).as("User has at least two orders").hasSizeGreaterThanOrEqualTo(2);
        assertThat(orders)
                .extracting(OrderSummary::number)
                .as("New orders are in history")
                .containsAll(createdOrderNumbers);

        for (OrderSummary order :
                orders.stream()
                        .filter(order -> createdOrderNumbers.contains(order.number()))
                        .toList()) {
            assertOrderDetails(order);
        }
    }

    private void assertOrderDetails(OrderSummary order) {
        assertThat(order.date()).as("Order %s date", order.number()).isNotNull();
        assertThat(order.total()).as("Order %s total", order.number()).isPositive();
        assertThat(order.status()).as("Order %s status", order.number()).isNotBlank();
    }
}
