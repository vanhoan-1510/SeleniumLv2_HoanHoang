package com.hoanhoang.data.model;

import java.math.BigDecimal;

public record CartItem(String name, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}
