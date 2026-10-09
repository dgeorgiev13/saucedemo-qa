package com.saucedemo.model;

import java.math.BigDecimal;
import java.util.Comparator;

/** A product as displayed on the inventory and cart pages. */
public record Product(String name, BigDecimal price) {

    public static final Comparator<Product> BY_PRICE = Comparator.comparing(Product::price);
    public static final Comparator<Product> BY_NAME = Comparator.comparing(Product::name, String::compareToIgnoreCase);
}
