package com.saucedemo.utils;

import com.saucedemo.config.TestConfig;
import com.saucedemo.model.Credentials;
import com.saucedemo.pages.LoginPage;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the credentials of the standard user without hard-coding them.
 * <ol>
 *     <li>Explicit override: {@code SAUCE_USERNAME} / {@code SAUCE_PASSWORD} (env) or
 *     {@code -Dsauce.username} / {@code -Dsauce.password} (system properties). Useful when the
 *     credentials are rotated or the tests run against an environment that does not publish them.</li>
 *     <li>Otherwise they are read from the login page itself, which publishes the accepted usernames
 *     and the shared password. The standard user is the one whose name starts with the configured
 *     prefix ({@code credentials.standard.user.prefix}).</li>
 * </ol>
 */
public final class CredentialsProvider {

    private CredentialsProvider() {
    }

    public static Credentials standardUser(LoginPage loginPage) {
        Optional<String> username = TestConfig.optional("sauce.username");
        Optional<String> password = TestConfig.optional("sauce.password");
        if (username.isPresent() && password.isPresent()) {
            return new Credentials(username.get(), password.get());
        }
        return fromLoginPage(loginPage);
    }

    private static Credentials fromLoginPage(LoginPage loginPage) {
        String prefix = TestConfig.standardUserPrefix();
        List<String> usernames = loginPage.acceptedUsernames();
        List<String> standardUsers = usernames.stream()
                .filter(name -> name.startsWith(prefix))
                .toList();
        if (standardUsers.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one username starting with '%s' on the login page, found %s in %s"
                            .formatted(prefix, standardUsers, usernames));
        }
        return new Credentials(standardUsers.getFirst(), loginPage.publishedPassword());
    }
}
