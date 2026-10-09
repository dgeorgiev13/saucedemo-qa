package com.saucedemo.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.LoginPage;

/** Header shown on every page after login: shopping cart and burger menu. */
public class HeaderComponent {

    private final Page page;
    private final Locator cartLink;
    private final Locator cartBadge;
    private final Locator openMenuButton;
    private final Locator logoutLink;

    public HeaderComponent(Page page) {
        this.page = page;
        this.cartLink = page.getByTestId("shopping-cart-link");
        this.cartBadge = page.getByTestId("shopping-cart-badge");
        this.openMenuButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Open Menu"));
        this.logoutLink = page.getByTestId("logout-sidebar-link");
    }

    /** Number of items in the cart; the badge is not rendered when the cart is empty. */
    public Locator cartBadge() {
        return cartBadge;
    }

    public CartPage openCart() {
        cartLink.click();
        return new CartPage(page);
    }

    public LoginPage logout() {
        openMenuButton.click();
        logoutLink.click();
        return new LoginPage(page);
    }
}
