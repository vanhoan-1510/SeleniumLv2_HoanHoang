package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.pages.support.ApplicationWait;
import org.openqa.selenium.By;

public class BillingFormComponent {
    private final Element firstNameInput = $(By.id("billing_first_name"));
    private final Element lastNameInput = $(By.id("billing_last_name"));
    private final Element addressInput = $(By.id("billing_address_1"));
    private final Element cityInput = $(By.id("billing_city"));
    private final Element postcodeInput = $(By.id("billing_postcode"));
    private final Element postcodeField = $(By.id("billing_postcode_field"));
    private final Element phoneInput = $(By.id("billing_phone"));
    private final Element emailInput = $(By.id("billing_email"));

    public void fill(BillingDetails billing) {
        type(firstNameInput, billing.firstName());
        type(lastNameInput, billing.lastName());
        type(addressInput, billing.address());
        type(cityInput, billing.city());
        type(postcodeInput, billing.postcode());
        type(phoneInput, billing.phone());
        if (emailInput.isVisible()) {
            type(emailInput, billing.email());
        }
        ApplicationWait.idle();
    }

    public boolean isPostcodeRequired() {
        return postcodeField.readSnapshot(
                field ->
                        field.isVisible()
                                && String.valueOf(field.getDomAttribute("class"))
                                        .contains("validate-required"));
    }

    private void type(Element input, String value) {
        input.type(value);
        input.blur();
    }
}
