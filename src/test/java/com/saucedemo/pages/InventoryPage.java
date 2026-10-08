package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.saucedemo.pages.components.HeaderComponent;
import com.saucedemo.model.Product;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class InventoryPage extends BasePage {

    private static final String PATH = "/inventory.html";

    private final HeaderComponent header;
    private final Locator items = byTestId("inventory-item");
    private final Locator sortDropdown = byTestId("product-sort-container");
    private final Locator activeSortOption = byTestId("active-option");

    public InventoryPage(Page page) {
        super(page);
        this.header = new HeaderComponent(page);
    }

    public InventoryPage waitUntilLoaded() {
        waitForPath(PATH);
        assertThat(items.first()).isVisible();
        return this;
    }

    public HeaderComponent header() {
        return header;
    }

    public List<Product> products() {
        return readProducts(items);
    }

    public InventoryPage sortBy(SortOption option) {
        sortDropdown.selectOption(option.value());
        assertThat(activeSortOption).hasText(option.label());
        return this;
    }

    public InventoryPage addToCart(Product product) {
        itemByName(items, product.name())
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add to cart"))
                .click();
        assertThat(removeButton(product)).isVisible();
        return this;
    }

    private Locator removeButton(Product product) {
        return itemByName(items, product.name())
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove"));
    }

    public enum SortOption {
        NAME_A_TO_Z("az", "Name (A to Z)"),
        NAME_Z_TO_A("za", "Name (Z to A)"),
        PRICE_LOW_TO_HIGH("lohi", "Price (low to high)"),
        PRICE_HIGH_TO_LOW("hilo", "Price (high to low)");

        private final String value;
        private final String label;

        SortOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String value() {
            return value;
        }

        public String label() {
            return label;
        }
    }
}
