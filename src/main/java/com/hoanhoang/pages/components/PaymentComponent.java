package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;
import static com.hoanhoang.core.element.ElementFactory.$$;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.pages.support.ApplicationWait;
import org.openqa.selenium.By;

public class PaymentComponent {
    private static final String PAYMENT_LABEL_SELECTOR = "label[for='%s']";
    private final Element bankTransferOption = $(By.id("payment_method_bacs"));
    private final Element chequeOption = $(By.id("payment_method_cheque"));
    private final Element cashOnDeliveryOption = $(By.id("payment_method_cod"));
    private final Elements selectedOptions =
            $$(By.cssSelector("input[name='payment_method']:checked"));

    public void select(PaymentMethod method) {
        ApplicationWait.idle();
        Element option = paymentOption(method);
        option.waitUntilPresent();
        if (!option.isSelected()) {
            if (option.isVisible()) {
                option.click();
            } else {
                paymentLabel(option.getDomAttribute("id")).click();
            }
        }
        option.waitUntilSelected();
        ApplicationWait.idle();
    }

    public String getSelectedMethod() {
        ApplicationWait.idle();
        return selectedOptions.waitUntil(
                "Selected payment method label",
                selected -> {
                    if (selected.isEmpty()) {
                        return null;
                    }
                    String id = selected.getFirst().getDomAttribute("id");
                    return paymentLabel(id).readSnapshot(Element::getText);
                });
    }

    private Element paymentOption(PaymentMethod method) {
        return switch (method) {
            case BANK_TRANSFER -> bankTransferOption;
            case CHEQUE -> chequeOption;
            case COD -> cashOnDeliveryOption;
        };
    }

    private Element paymentLabel(String optionId) {
        return $(By.cssSelector(PAYMENT_LABEL_SELECTOR.formatted(optionId)));
    }
}
