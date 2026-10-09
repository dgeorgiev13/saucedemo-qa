package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** "Checkout: Overview" step. */
public class CheckoutOverviewPage extends BasePage {

    private final Locator finishButton = byTestId("finish");

    public CheckoutOverviewPage(Page page) {
        super(page);
        waitForUrl("/checkout-step-two.html");
    }

    public CheckoutCompletePage finish() {
        finishButton.click();
        return new CheckoutCompletePage(page);
    }
}
