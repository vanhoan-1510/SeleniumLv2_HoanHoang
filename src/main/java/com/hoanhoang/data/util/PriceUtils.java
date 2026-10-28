package com.hoanhoang.data.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conversion at the UI boundary for the demo site's USD price format. */
public final class PriceUtils {
    private static final Pattern USD_PRICE =
            Pattern.compile("\\$\\s*([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)(?![0-9.,])");

    private PriceUtils() {}

    public static BigDecimal parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Price text must not be null");
        }
        Matcher matcher = USD_PRICE.matcher(text.replace('\u00a0', ' '));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse USD price: " + text);
        }
        BigDecimal amount = new BigDecimal(matcher.group(1).replace(",", ""));
        if (matcher.find()) {
            throw new IllegalArgumentException(
                    "Expected one current price, but found multiple prices: " + text);
        }
        return amount;
    }

    public static String format(BigDecimal amount) {
        return new DecimalFormat("$#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
                .format(amount);
    }
}
