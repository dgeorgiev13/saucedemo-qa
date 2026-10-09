package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.saucedemo.model.Product;
import com.saucedemo.pages.components.HeaderComponent;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class InventoryPage extends BasePage {

    private final HeaderComponent header;
    private final Locator items = byTestId("inventory-item");
    private final Locator sortDropdown = byTestId("product-sort-container");
    private final Locator activeSortOption = byTestId("active-option");

    public InventoryPage(Page page) {
        super(page);
        this.header = new HeaderComponent(page);
        waitForUrl("/inventory.html");
    }

    public HeaderComponent header() {
        return header;
    }

    public List<Product> products() {
        assertThat(items.first()).isVisible();
        return readProducts(items);
    }

    public InventoryPage sortBy(SortOption option) {
        sortDropdown.selectOption(new SelectOption().setLabel(option.label));
        assertThat(activeSortOption).hasText(option.label);
        return this;
    }

    public InventoryPage addToCart(Product product) {
        Locator item = itemByName(items, product.name());
        item.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add to cart")).click();
        assertThat(item.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove"))).isVisible();
        return this;
    }

    /** Options of the product sort dropdown, by their visible label. */
    public enum SortOption {
        NAME_A_TO_Z("Name (A to Z)"),
        NAME_Z_TO_A("Name (Z to A)"),
        PRICE_LOW_TO_HIGH("Price (low to high)"),
        PRICE_HIGH_TO_LOW("Price (high to low)");

        private final String label;

        SortOption(String label) {
            this.label = label;
        }
    }
}
