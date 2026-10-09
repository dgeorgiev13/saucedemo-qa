package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.config.TestConfig;
import com.saucedemo.model.Credentials;

import java.util.Arrays;
import java.util.List;

public class LoginPage extends BasePage {

    private final Locator usernameInput = byTestId("username");
    private final Locator passwordInput = byTestId("password");
    private final Locator loginButton = byTestId("login-button");
    private final Locator acceptedUsernames = byTestId("login-credentials");
    private final Locator passwordForAllUsers = byTestId("login-password");

    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open() {
        page.navigate(TestConfig.baseUrl());
        return this;
    }

    public Locator loginButton() {
        return loginButton;
    }

    public InventoryPage loginAs(Credentials credentials) {
        usernameInput.fill(credentials.username());
        passwordInput.fill(credentials.password());
        loginButton.click();
        return new InventoryPage(page);
    }

    /** Usernames published on the login page under "Accepted usernames are:". */
    public List<String> acceptedUsernames() {
        return linesWithoutHeading(acceptedUsernames);
    }

    /** Password published on the login page under "Password for all users:". */
    public String publishedPassword() {
        return linesWithoutHeading(passwordForAllUsers).getFirst();
    }

    private static List<String> linesWithoutHeading(Locator block) {
        String heading = block.locator("h4").innerText();
        return Arrays.stream(block.innerText().split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.equals(heading))
                .toList();
    }
}
