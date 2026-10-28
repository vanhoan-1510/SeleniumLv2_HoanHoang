package com.hoanhoang.data.model;

import java.math.BigDecimal;

public record OrderItem(String name, int quantity, BigDecimal total) {}
