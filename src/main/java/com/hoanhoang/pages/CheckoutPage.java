package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
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

public class CheckoutPage extends BasePage {
    private static final By VALIDATION_ERRORS = By.cssSelector(".woocommerce-error li");

    private final BillingFormComponent billing = new BillingFormComponent();
    private final PaymentComponent payment = new PaymentComponent();
    private final OrderItemsComponent order =
            new OrderItemsComponent(By.cssSelector("#order_review tbody tr.cart_item"));
    private final Element validationError = $(VALIDATION_ERRORS);
    private final Elements errorMessages = $$(VALIDATION_ERRORS);
    private final Elements invalidBillingFields =
            $$(
                    By.cssSelector(
                            ".woocommerce-billing-fields .form-row.woocommerce-invalid[id^='billing_']"));
    private final Element confirmation = $(By.cssSelector(".woocommerce-thankyou-order-received"));
    private final Element checkoutForm = $(By.cssSelector("form.checkout"));
    private final Element placeOrderButton = $(By.id("place_order"));

    @Step("Verify Checkout page is displayed")
    public void waitUntilLoaded() {
        checkoutForm.waitUntil(
                "Checkout form displayed",
                form -> {
                    ApplicationWait.failIfCriticalError();
                    return form.isVisible();
                });
        ApplicationWait.idle();
    }

    public boolean isLoaded() {
        return checkoutForm.isVisible();
    }

    public boolean isPostcodeRequired() {
        return billing.isPostcodeRequired();
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
        placeOrderButton.click();
        UiWait.until(
                "Checkout response: order confirmation or validation errors",
                d -> {
                    ApplicationWait.failIfCriticalError();
                    return d.getCurrentUrl().contains("order-received")
                            || confirmation.isVisible()
                            || validationError.isVisible();
                });
        ApplicationWait.idle();
    }

    public List<String> getErrorMessages() {
        return errorMessages.visible().map(Element::getText);
    }

    public List<String> getInvalidBillingFieldIds() {
        return invalidBillingFields
                .visible()
                .map(field -> field.getDomAttribute("id").replaceFirst("_field$", ""));
    }

    public Map<String, String> getFieldErrorMessages() {
        return errorMessages
                .visible()
                .readSnapshot(
                        messages -> {
                            Map<String, String> result = new LinkedHashMap<>();
                            for (Element error : messages) {
                                String field = error.getDomAttribute("data-id");
                                if (field != null && !field.isBlank()) {
                                    result.put(field, error.getText());
                                }
                            }
                            return Map.copyOf(result);
                        });
    }
}
