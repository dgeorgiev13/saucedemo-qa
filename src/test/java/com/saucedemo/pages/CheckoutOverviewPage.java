package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.model.Product;
import com.saucedemo.utils.PriceParser;

import java.math.BigDecimal;
import java.util.List;

/** "Checkout: Overview" step. */
public class CheckoutOverviewPage extends BasePage {

    private static final String PATH = "/checkout-step-two.html";

    private final Locator items = byTestId("inventory-item");
    private final Locator itemTotalLabel = byTestId("subtotal-label");
    private final Locator finishButton = byTestId("finish");

    public CheckoutOverviewPage(Page page) {
        super(page);
    }

    public CheckoutOverviewPage waitUntilLoaded() {
        waitForPath(PATH);
        return this;
    }

    public List<Product> products() {
        return readProducts(items);
    }

    /** "Item total" - the sum of item prices before tax. */
    public BigDecimal itemTotal() {
        return PriceParser.parse(itemTotalLabel.innerText());
    }

    public CheckoutCompletePage finish() {
        finishButton.click();
        return new CheckoutCompletePage(page).waitUntilLoaded();
    }
}
