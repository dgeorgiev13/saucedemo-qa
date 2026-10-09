package com.saucedemo.utils;

import com.saucedemo.config.TestConfig;
import com.saucedemo.model.Credentials;
import com.saucedemo.pages.LoginPage;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the standard user credentials without hard-coding them:
 * <ol>
 *     <li>{@code SAUCE_USERNAME} / {@code SAUCE_PASSWORD} (env) or {@code -Dsauce.username} / {@code -Dsauce.password},
 *     e.g. CI secrets;</li>
 *     <li>otherwise read from the login page, which publishes the accepted usernames and the shared password.</li>
 * </ol>
 */
public final class CredentialsProvider {

    private CredentialsProvider() {
    }

    public static Credentials standardUser(LoginPage loginPage) {
        Optional<String> username = TestConfig.find("sauce.username");
        Optional<String> password = TestConfig.find("sauce.password");
        if (username.isPresent() && password.isPresent()) {
            return new Credentials(username.get(), password.get());
        }

        String prefix = TestConfig.standardUserPrefix();
        List<String> standardUsers = loginPage.acceptedUsernames().stream()
                .filter(name -> name.startsWith(prefix))
                .toList();
        if (standardUsers.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one published username starting with '%s', found: %s".formatted(prefix, standardUsers));
        }
        return new Credentials(standardUsers.getFirst(), loginPage.publishedPassword());
    }
}
