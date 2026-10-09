package com.saucedemo.model;

import com.saucedemo.config.TestConfig;

import java.util.Properties;

/** Customer data entered on the "Checkout: Your Information" step. */
public record CheckoutInfo(String firstName, String lastName, String postalCode) {

    public static CheckoutInfo fromTestData() {
        Properties data = TestConfig.loadProperties("testdata/checkout.properties");
        return new CheckoutInfo(
                data.getProperty("first.name"),
                data.getProperty("last.name"),
                data.getProperty("postal.code"));
    }
}
