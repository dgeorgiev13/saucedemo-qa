package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.saucedemo.model.Product;
import com.saucedemo.pages.components.HeaderComponent;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CartPage extends BasePage {

    private static final String PATH = "/cart.html";

    private final HeaderComponent header;
    private final Locator items = byTestId("inventory-item");
    private final Locator continueShoppingButton = byTestId("continue-shopping");
    private final Locator checkoutButton = byTestId("checkout");

    public CartPage(Page page) {
        super(page);
        this.header = new HeaderComponent(page);
    }

    public CartPage waitUntilLoaded() {
        waitForPath(PATH);
        return this;
    }

    public HeaderComponent header() {
        return header;
    }

    public Locator items() {
        return items;
    }

    public List<Product> products() {
        return readProducts(items);
    }

    public CartPage remove(Product product) {
        Locator item = itemByName(items, product.name());
        item.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove")).click();
        assertThat(item).hasCount(0);
        return this;
    }

    public InventoryPage continueShopping() {
        continueShoppingButton.click();
        return new InventoryPage(page).waitUntilLoaded();
    }

    public CheckoutInformationPage checkout() {
        checkoutButton.click();
        return new CheckoutInformationPage(page).waitUntilLoaded();
    }
}
