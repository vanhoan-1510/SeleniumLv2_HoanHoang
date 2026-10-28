package com.hoanhoang.data.model;

import java.util.Objects;

/** Checkout billing form data. Mapped by Gson from JSON or created with BillingBuilder. */
public record BillingDetails(
        String firstName,
        String lastName,
        String address,
        String city,
        String postcode,
        String phone,
        String email) {
    public BillingDetails {
        Objects.requireNonNull(firstName, "billing firstName");
        Objects.requireNonNull(lastName, "billing lastName");
        Objects.requireNonNull(address, "billing address");
        Objects.requireNonNull(city, "billing city");
        Objects.requireNonNull(postcode, "billing postcode");
        Objects.requireNonNull(phone, "billing phone");
        Objects.requireNonNull(email, "billing email");
    }
}
