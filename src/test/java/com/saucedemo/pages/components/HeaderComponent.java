package com.saucedemo.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.LoginPage;

/** Primary header shown on every page after login: burger menu and shopping cart. */
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

    /** Badge with the number of items; it is not rendered at all when the cart is empty. */
    public Locator cartBadge() {
        return cartBadge;
    }

    public CartPage openCart() {
        cartLink.click();
        return new CartPage(page).waitUntilLoaded();
    }

    public LoginPage logout() {
        openMenuButton.click();
        logoutLink.click();
        return new LoginPage(page).waitUntilLoaded();
    }
}
