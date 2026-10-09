package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.model.Product;

import java.math.BigDecimal;
import java.util.List;

/** Common behaviour for all page objects. Locators rely on the application's {@code data-test} attributes. */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected Locator byTestId(String testId) {
        return page.getByTestId(testId);
    }

    protected void waitForUrl(String path) {
        page.waitForURL("**" + path);
    }

    /** Reads name and price of every item - inventory and cart pages share the same item markup. */
    protected static List<Product> readProducts(Locator items) {
        return items.all().stream()
                .map(item -> new Product(
                        item.getByTestId("inventory-item-name").innerText(),
                        parsePrice(item.getByTestId("inventory-item-price").innerText())))
                .toList();
    }

    /** The item whose product name matches exactly (no partial matches between similar names). */
    protected Locator itemByName(Locator items, String productName) {
        return items.filter(new Locator.FilterOptions().setHas(
                page.getByTestId("inventory-item-name")
                        .getByText(productName, new Locator.GetByTextOptions().setExact(true))));
    }

    /** "$29.99" -> 29.99 */
    private static BigDecimal parsePrice(String text) {
        return new BigDecimal(text.replace("$", "").trim());
    }
}
