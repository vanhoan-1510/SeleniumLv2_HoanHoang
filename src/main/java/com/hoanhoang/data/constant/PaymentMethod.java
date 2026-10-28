package com.hoanhoang.data.constant;

public enum PaymentMethod {
    BANK_TRANSFER("Direct bank transfer"),
    CHEQUE("Check payments"),
    COD("Cash on delivery");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
