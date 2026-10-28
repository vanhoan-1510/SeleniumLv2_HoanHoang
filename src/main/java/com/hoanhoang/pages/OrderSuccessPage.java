package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.pages.components.OrderItemsComponent;
import com.hoanhoang.pages.support.ApplicationWait;
import java.util.List;
import org.openqa.selenium.By;

public class OrderSuccessPage extends BasePage {
    private final By confirmation = By.cssSelector(".woocommerce-thankyou-order-received");
    private final OrderItemsComponent items =
            new OrderItemsComponent(By.cssSelector("table.order_details tbody tr.order_item"));

    public void waitUntilLoaded() {
        UiWait.visible(confirmation);
        ApplicationWait.idle();
    }

    public boolean isLoaded() {
        return UiWait.isVisible(confirmation);
    }

    public Element confirmationMessage() {
        return $(confirmation);
    }

    public List<OrderItem> getProducts() {
        return items.getItems();
    }

    public String getBillingAddress() {
        return UiWait.visible(
                        By.cssSelector(
                                ".woocommerce-column--billing-address address, .woocommerce-customer-details address"))
                .getText()
                .trim();
    }

    public String getOrderNumber() {
        return UiWait.visible(By.cssSelector("li.woocommerce-order-overview__order strong"))
                .getText()
                .replace("#", "")
                .trim();
    }

    public String getPaymentMethod() {
        return UiWait.visible(By.xpath("//tr[th[normalize-space()='Payment method:']]/td"))
                .getText()
                .trim();
    }
}
