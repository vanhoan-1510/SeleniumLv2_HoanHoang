package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.pages.components.OrderItemsComponent;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;
import java.util.List;
import org.openqa.selenium.By;

public class OrderSuccessPage extends BasePage {
    private static final By CONFIRMATION = By.cssSelector(".woocommerce-thankyou-order-received");
    private final Element billingAddress =
            $(
                    By.cssSelector(
                            ".woocommerce-column--billing-address address, .woocommerce-customer-details address"));
    private final Element orderNumber =
            $(By.cssSelector("li.woocommerce-order-overview__order strong"));
    private final Element paymentMethod =
            $(By.xpath("//tr[th[normalize-space()='Payment method:']]/td"));
    private final OrderItemsComponent items =
            new OrderItemsComponent(By.cssSelector("table.order_details tbody tr.order_item"));

    @Step("Verify Order Status page is displayed")
    public void waitUntilLoaded() {
        $(CONFIRMATION).waitUntilVisible();
        ApplicationWait.idle();
    }

    public Element confirmationMessage() {
        return $(CONFIRMATION);
    }

    public List<OrderItem> getProducts() {
        return items.getItems();
    }

    public String getBillingAddress() {
        return billingAddress.getText().trim();
    }

    public String getOrderNumber() {
        return orderNumber.getText().replace("#", "").trim();
    }

    public String getPaymentMethod() {
        return paymentMethod.getText().trim();
    }
}
