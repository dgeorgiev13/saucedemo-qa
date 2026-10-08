package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.config.TestConfig;
import com.saucedemo.model.Credentials;

import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BasePage {

    private final Locator usernameInput = byTestId("username");
    private final Locator passwordInput = byTestId("password");
    private final Locator loginButton = byTestId("login-button");
    private final Locator errorMessage = byTestId("error");
    private final Locator acceptedUsernamesBlock = byTestId("login-credentials");
    private final Locator passwordBlock = byTestId("login-password");

    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open() {
        page.navigate(TestConfig.baseUrl());
        return waitUntilLoaded();
    }

    public LoginPage waitUntilLoaded() {
        assertThat(loginButton).isVisible();
        return this;
    }

    public Locator loginButton() {
        return loginButton;
    }

    public Locator errorMessage() {
        return errorMessage;
    }

    public InventoryPage loginAs(Credentials credentials) {
        usernameInput.fill(credentials.username());
        passwordInput.fill(credentials.password());
        loginButton.click();
        return new InventoryPage(page).waitUntilLoaded();
    }

    /** Usernames published on the login page under "Accepted usernames are:". */
    public List<String> acceptedUsernames() {
        return valueLines(acceptedUsernamesBlock);
    }

    /** Password published on the login page under "Password for all users:". */
    public String publishedPassword() {
        List<String> values = valueLines(passwordBlock);
        if (values.size() != 1) {
            throw new IllegalStateException("Expected exactly one published password, found: " + values.size());
        }
        return values.getFirst();
    }

    /** Text lines of a credentials block without its heading. */
    private static List<String> valueLines(Locator block) {
        String heading = block.locator("h4").innerText().trim();
        return Arrays.stream(block.innerText().split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.equals(heading))
                .toList();
    }
}
