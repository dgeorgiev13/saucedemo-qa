package com.saucedemo.utils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts UI price labels such as "$29.99" or "Item total: $37.98" to {@link BigDecimal}. */
public final class PriceParser {

    private static final Pattern AMOUNT = Pattern.compile("\\$\\s*(\\d+(?:\\.\\d{1,2})?)");

    private PriceParser() {
    }

    public static BigDecimal parse(String text) {
        Matcher matcher = AMOUNT.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No price found in text: '" + text + "'");
        }
        return new BigDecimal(matcher.group(1));
    }
}
