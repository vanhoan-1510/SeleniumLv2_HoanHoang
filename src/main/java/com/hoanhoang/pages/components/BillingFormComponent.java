package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.pages.support.ApplicationWait;
import java.util.Arrays;
import java.util.Optional;
import org.openqa.selenium.By;

public class BillingFormComponent {
    public enum Field {
        FIRST_NAME("billing_first_name"),
        LAST_NAME("billing_last_name"),
        ADDRESS("billing_address_1"),
        CITY("billing_city"),
        POSTCODE("billing_postcode"),
        PHONE("billing_phone"),
        EMAIL("billing_email");

        private final String inputId;

        Field(String inputId) {
            this.inputId = inputId;
        }

        public static Optional<Field> fromInputId(String inputId) {
            return Arrays.stream(values())
                    .filter(field -> field.inputId.equals(inputId))
                    .findFirst();
        }
    }

    private final Element firstNameInput = $(By.id(Field.FIRST_NAME.inputId));
    private final Element lastNameInput = $(By.id(Field.LAST_NAME.inputId));
    private final Element addressInput = $(By.id(Field.ADDRESS.inputId));
    private final Element cityInput = $(By.id(Field.CITY.inputId));
    private final Element postcodeInput = $(By.id(Field.POSTCODE.inputId));
    private final Element postcodeField = $(By.id(Field.POSTCODE.inputId + "_field"));
    private final Element phoneInput = $(By.id(Field.PHONE.inputId));
    private final Element emailInput = $(By.id(Field.EMAIL.inputId));

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
