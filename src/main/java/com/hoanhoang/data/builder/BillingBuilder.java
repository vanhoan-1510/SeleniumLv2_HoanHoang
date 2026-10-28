package com.hoanhoang.data.builder;

import com.hoanhoang.data.model.BillingDetails;

/**
 * Builds BillingDetails variants for tests, e.g. blank() for mandatory-field validation or
 * from(defaults).withEmail(...) to change a single field.
 */
public final class BillingBuilder {
    private String firstName = "";
    private String lastName = "";
    private String address = "";
    private String city = "";
    private String postcode = "";
    private String phone = "";
    private String email = "";

    private BillingBuilder() {}

    /** All fields empty. */
    public static BillingBuilder blank() {
        return new BillingBuilder();
    }

    public static BillingBuilder from(BillingDetails source) {
        return new BillingBuilder()
                .withFirstName(source.firstName())
                .withLastName(source.lastName())
                .withAddress(source.address())
                .withCity(source.city())
                .withPostcode(source.postcode())
                .withPhone(source.phone())
                .withEmail(source.email());
    }

    public BillingBuilder withFirstName(String value) {
        this.firstName = value;
        return this;
    }

    public BillingBuilder withLastName(String value) {
        this.lastName = value;
        return this;
    }

    public BillingBuilder withAddress(String value) {
        this.address = value;
        return this;
    }

    public BillingBuilder withCity(String value) {
        this.city = value;
        return this;
    }

    public BillingBuilder withPostcode(String value) {
        this.postcode = value;
        return this;
    }

    public BillingBuilder withPhone(String value) {
        this.phone = value;
        return this;
    }

    public BillingBuilder withEmail(String value) {
        this.email = value;
        return this;
    }

    public BillingDetails build() {
        return new BillingDetails(firstName, lastName, address, city, postcode, phone, email);
    }
}
