package com.saucedemo.model;

import com.saucedemo.config.TestConfig.PropertiesLoader;

import java.util.Properties;

/** Customer data entered on the "Checkout: Your Information" step. */
public record CheckoutInfo(String firstName, String lastName, String postalCode) {

    private static final String TEST_DATA_FILE = "testdata/checkout.properties";

    public static CheckoutInfo fromTestData() {
        Properties data = PropertiesLoader.load(TEST_DATA_FILE);
        return new CheckoutInfo(
                data.getProperty("first.name"),
                data.getProperty("last.name"),
                data.getProperty("postal.code"));
    }
}
