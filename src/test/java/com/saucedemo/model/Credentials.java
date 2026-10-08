package com.saucedemo.model;

/** Login credentials. {@link #toString()} masks the password so it never ends up in logs or reports. */
public record Credentials(String username, String password) {

    @Override
    public String toString() {
        return "Credentials[username=%s, password=****]".formatted(username);
    }
}
