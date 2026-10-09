package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.pages.components.HeaderComponent;

/** "Checkout: Complete!" step. */
public class CheckoutCompletePage extends BasePage {

    private final HeaderComponent header;
    private final Locator completeHeader = byTestId("complete-header");

    public CheckoutCompletePage(Page page) {
        super(page);
        this.header = new HeaderComponent(page);
        waitForUrl("/checkout-complete.html");
    }

    public HeaderComponent header() {
        return header;
    }

    public Locator completeHeader() {
        return completeHeader;
    }
}
