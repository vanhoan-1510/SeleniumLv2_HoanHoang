package com.hoanhoang.data.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderSummary(String number, LocalDate date, BigDecimal total, String status) {}
