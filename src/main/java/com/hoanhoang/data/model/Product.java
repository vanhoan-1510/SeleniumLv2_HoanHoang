package com.hoanhoang.data.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Immutable snapshot of a selected catalog product; price is the current unit price. */
public record Product(String name, BigDecimal price) {
    public Product {
        Objects.requireNonNull(name, "Product name must not be null");
        Objects.requireNonNull(price, "Product price must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Product price must not be negative");
        }
    }
}
