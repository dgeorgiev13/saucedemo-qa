package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.model.CheckoutInfo;
import com.saucedemo.model.Credentials;
import com.saucedemo.model.Product;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.CheckoutCompletePage;
import com.saucedemo.pages.InventoryPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.utils.CredentialsProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.saucedemo.pages.InventoryPage.SortOption.NAME_A_TO_Z;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two kinds of assertions: Playwright {@code assertThat(Locator)} for UI state (auto-waiting)
 * and AssertJ {@code assertThat(List/Object)} for data read from the UI.
 */
class PurchaseFlowTest extends BaseTest {

    @Test
    @DisplayName("Standard user buys the cheapest product and the last product by name, then logs out")
    void standardUserCompletesPurchaseFlow() {
        // 1. Obtain the standard user credentials
        LoginPage loginPage = new LoginPage(page).open();
        Credentials credentials = CredentialsProvider.standardUser(loginPage);

        // 2. Log in
        InventoryPage inventory = loginPage.loginAs(credentials);

        // 3. Add the products with the highest and the lowest price
        List<Product> products = inventory.products();
        Product mostExpensive = products.stream().max(Product.BY_PRICE).orElseThrow();
        Product cheapest = products.stream().min(Product.BY_PRICE).orElseThrow();

        inventory.addToCart(mostExpensive).addToCart(cheapest);
        CartPage cart = inventory.header().openCart();
        assertThat(cart.products()).containsExactlyInAnyOrder(mostExpensive, cheapest);

        // 4. Remove the product with the highest price and verify the cart content
        cart.remove(mostExpensive);
        assertThat(cart.products()).containsExactly(cheapest);

        // 5. Sort by Name (A to Z) and add the last product
        inventory = cart.continueShopping().sortBy(NAME_A_TO_Z);
        List<Product> sortedProducts = inventory.products();
        assertThat(sortedProducts).isSortedAccordingTo(Product.BY_NAME);

        Product lastByName = sortedProducts.getLast();
        inventory.addToCart(lastByName);

        // 6. Go to checkout
        cart = inventory.header().openCart();
        assertThat(cart.products()).containsExactlyInAnyOrder(cheapest, lastByName);

        // 7. Finish the order
        CheckoutCompletePage complete = cart.checkout()
                .continueWith(CheckoutInfo.fromTestData())
                .finish();

        // 8. Verify the order is placed
        assertThat(complete.completeHeader()).hasText("Thank you for your order!");

        // 9. Verify the cart is empty
        assertThat(complete.header().cartBadge()).isHidden();
        assertThat(complete.header().openCart().items()).hasCount(0);

        // 10. Log out
        LoginPage loggedOut = complete.header().logout();
        assertThat(loggedOut.loginButton()).isVisible();
    }
}
