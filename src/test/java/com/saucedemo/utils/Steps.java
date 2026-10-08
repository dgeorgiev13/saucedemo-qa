package com.saucedemo.utils;

/** Minimal step logger, so the console output of a test reads like the scenario it automates. */
public final class Steps {

    private Steps() {
    }

    public static void step(String description) {
        System.out.println("STEP " + description);
    }
}
