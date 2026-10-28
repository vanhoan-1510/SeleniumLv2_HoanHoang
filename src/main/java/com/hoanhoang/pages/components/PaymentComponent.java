package com.hoanhoang.pages.components;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.support.ApplicationWait;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class PaymentComponent {
    private By locator(PaymentMethod method) {
        return By.id(
                switch (method) {
                    case BANK_TRANSFER -> "payment_method_bacs";
                    case CHEQUE -> "payment_method_cheque";
                    case COD -> "payment_method_cod";
                });
    }

    public void select(PaymentMethod method) {
        ApplicationWait.idle();
        By input = locator(method);
        WebElement option =
                UiWait.until(
                        "Payment option present: " + method,
                        d -> d.findElements(input).stream().findFirst().orElse(null));
        if (!option.isSelected()) {
            if (option.isDisplayed()) {
                UiWait.click(input);
            } else {
                UiWait.click(By.cssSelector("label[for='" + option.getAttribute("id") + "']"));
            }
        }
        UiWait.until("Payment method selected: " + method, d -> d.findElement(input).isSelected());
        ApplicationWait.idle();
    }

    public String getSelectedMethod() {
        ApplicationWait.idle();
        return UiWait.until(
                "Selected payment method label",
                d -> {
                    List<WebElement> selected =
                            d.findElements(By.cssSelector("input[name='payment_method']:checked"));
                    if (selected.isEmpty()) {
                        return null;
                    }
                    String id = selected.get(0).getAttribute("id");
                    return d.findElement(By.cssSelector("label[for='" + id + "']"))
                            .getText()
                            .trim();
                });
    }
}
