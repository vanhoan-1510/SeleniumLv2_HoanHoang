package com.hoanhoang.pages;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.pages.components.BillingFormComponent;
import com.hoanhoang.pages.components.OrderItemsComponent;
import com.hoanhoang.pages.components.PaymentComponent;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CheckoutPage extends BasePage {
    private final BillingFormComponent billing = new BillingFormComponent();
    private final PaymentComponent payment = new PaymentComponent();
    private final OrderItemsComponent order =
            new OrderItemsComponent(By.cssSelector("#order_review tbody tr.cart_item"));
    private final By errors = By.cssSelector(".woocommerce-error li");

    public void waitUntilLoaded() {
        UiWait.visible(By.cssSelector("form.checkout"));
        ApplicationWait.idle();
    }

    public boolean isLoaded() {
        return UiWait.isVisible(By.cssSelector("form.checkout"));
    }

    public List<OrderItem> getProducts() {
        return order.getItems();
    }

    @Step("Fill billing details")
    public void fillBillingDetails(BillingDetails details) {
        billing.fill(details);
    }

    @Step("Select payment method: {method}")
    public void selectPaymentMethod(PaymentMethod method) {
        payment.select(method);
    }

    public String getSelectedPaymentMethod() {
        return payment.getSelectedMethod();
    }

    @Step("Click 'Place order'")
    public void placeOrder() {
        ApplicationWait.idle();
        UiWait.click(By.id("place_order"));
        UiWait.until(
                "Checkout response: order confirmation or validation errors",
                d ->
                        d.getCurrentUrl().contains("order-received")
                                || UiWait.isVisible(
                                        By.cssSelector(".woocommerce-thankyou-order-received"))
                                || UiWait.isVisible(errors));
        ApplicationWait.idle();
    }

    public List<String> getErrorMessages() {
        return UiWait.visibleElements(errors).stream().map(e -> e.getText().trim()).toList();
    }

    public Map<String, String> getFieldErrors() {
        Map<String, String> result = new LinkedHashMap<>();
        for (WebElement error : UiWait.visibleElements(errors)) {
            String field = error.getAttribute("data-id");
            if (field != null && !field.isBlank()) {
                result.put(field, error.getText().trim());
            }
        }
        return Map.copyOf(result);
    }
}
