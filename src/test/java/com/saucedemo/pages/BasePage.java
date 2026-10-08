package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.model.Product;
import com.saucedemo.utils.PriceParser;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Common behaviour for all page objects. Locators rely on the application's {@code data-test} attributes. */
public abstract class BasePage {

    private static final String ITEM_NAME = "inventory-item-name";
    private static final String ITEM_PRICE = "inventory-item-price";

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected Locator byTestId(String testId) {
        return page.getByTestId(testId);
    }

    /** Waits (with Playwright auto-retry) until the browser URL ends with the given path. */
    protected void waitForPath(String path) {
        // The pattern is evaluated as a JavaScript RegExp, so Java's Pattern.quote (\Q...\E) cannot be used.
        String escapedPath = path.replaceAll("[.*+?^${}()|\\[\\]\\\\/]", "\\\\$0");
        assertThat(page).hasURL(Pattern.compile(escapedPath + "$"));
    }

    /** Reads products from item containers - inventory, cart and checkout overview share the same markup. */
    protected static List<Product> readProducts(Locator items) {
        return items.all().stream()
                .map(item -> new Product(
                        item.getByTestId(ITEM_NAME).innerText().trim(),
                        PriceParser.parse(item.getByTestId(ITEM_PRICE).innerText())))
                .toList();
    }

    /** The item container whose product name matches exactly (avoids partial matches between similar names). */
    protected Locator itemByName(Locator items, String productName) {
        return items.filter(new Locator.FilterOptions().setHas(
                page.getByTestId(ITEM_NAME).getByText(productName, new Locator.GetByTextOptions().setExact(true))));
    }
}
