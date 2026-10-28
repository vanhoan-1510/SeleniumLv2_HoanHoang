package com.hoanhoang.pages.components;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.pages.support.ApplicationWait;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class BillingFormComponent {
    public void fill(BillingDetails billing) {
        type("billing_first_name", billing.firstName());
        type("billing_last_name", billing.lastName());
        type("billing_address_1", billing.address());
        type("billing_city", billing.city());
        type("billing_postcode", billing.postcode());
        type("billing_phone", billing.phone());
        if (UiWait.isVisible(By.id("billing_email"))) {
            type("billing_email", billing.email());
        }
        ApplicationWait.idle();
    }

    private void type(String id, String value) {
        WebElement input = UiWait.clickable(By.id(id));
        input.clear();
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }
}
