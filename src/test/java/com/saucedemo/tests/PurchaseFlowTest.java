package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.model.CheckoutInfo;
import com.saucedemo.model.Credentials;
import com.saucedemo.model.Product;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.CheckoutCompletePage;
import com.saucedemo.pages.CheckoutOverviewPage;
import com.saucedemo.pages.InventoryPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.utils.CredentialsProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.saucedemo.pages.InventoryPage.SortOption.NAME_A_TO_Z;
import static com.saucedemo.utils.Steps.step;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * Assertions: Playwright {@code assertThat} for UI state (auto-retrying),
 * AssertJ {@code then} for data read from the UI (products, prices, sorting).
 */
class PurchaseFlowTest extends BaseTest {

    @Test
    @DisplayName("Standard user buys the cheapest product and the last product by name, then logs out")
    void standardUserCompletesPurchaseFlow() {
        step("1. Resolve the standard user credentials");
        LoginPage loginPage = new LoginPage(page).open();
        Credentials credentials = CredentialsProvider.standardUser(loginPage);

        step("2. Log in as " + credentials.username());
        InventoryPage inventory = loginPage.loginAs(credentials);

        step("3. Add the product with the highest price and the product with the lowest price");
        List<Product> products = inventory.products();
        Product mostExpensive = products.stream().max(Product.BY_PRICE).orElseThrow();
        Product cheapest = products.stream().min(Product.BY_PRICE).orElseThrow();
        then(mostExpensive).as("highest and lowest priced products must differ").isNotEqualTo(cheapest);

        inventory.addToCart(mostExpensive).addToCart(cheapest);
        assertThat(inventory.header().cartBadge()).hasText("2");

        CartPage cart = inventory.header().openCart();
        then(cart.products()).containsExactlyInAnyOrder(mostExpensive, cheapest);

        step("4. Remove the product with the highest price and verify the cart content");
        cart.remove(mostExpensive);
        then(cart.products()).containsExactly(cheapest);
        assertThat(cart.header().cartBadge()).hasText("1");

        step("5. Sort products by Name (A to Z) and add the last product");
        inventory = cart.continueShopping().sortBy(NAME_A_TO_Z);
        List<Product> sortedProducts = inventory.products();
        then(sortedProducts).isSortedAccordingTo(Product.BY_NAME);

        Product lastByName = sortedProducts.getLast();
        then(lastByName).as("last product by name is already in the cart").isNotEqualTo(cheapest);
        inventory.addToCart(lastByName);
        assertThat(inventory.header().cartBadge()).hasText("2");

        step("6. Go to checkout");
        List<Product> expectedOrder = List.of(cheapest, lastByName);
        cart = inventory.header().openCart();
        then(cart.products()).containsExactlyInAnyOrderElementsOf(expectedOrder);

        CheckoutOverviewPage overview = cart.checkout().continueWith(CheckoutInfo.fromTestData());
        then(overview.products()).containsExactlyInAnyOrderElementsOf(expectedOrder);
        then(overview.itemTotal()).isEqualByComparingTo(totalPrice(expectedOrder));

        step("7. Finish the order");
        CheckoutCompletePage complete = overview.finish();

        step("8. Verify the order is placed");
        assertThat(complete.completeHeader()).hasText(CheckoutCompletePage.SUCCESS_HEADER);

        step("9. Verify the cart is empty");
        assertThat(complete.header().cartBadge()).isHidden();
        CartPage emptyCart = complete.header().openCart();
        assertThat(emptyCart.items()).hasCount(0);

        step("10. Log out");
        LoginPage loggedOut = emptyCart.header().logout();
        assertThat(loggedOut.loginButton()).isVisible();

        // The session must really be gone: a protected page redirects back to the login form with an error.
        page.navigate("/inventory.html");
        assertThat(loggedOut.errorMessage()).containsText("You can only access '/inventory.html' when you are logged in");
    }

    private static BigDecimal totalPrice(List<Product> products) {
        return products.stream().map(Product::price).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
