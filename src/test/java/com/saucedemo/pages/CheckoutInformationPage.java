package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.model.CheckoutInfo;

/** "Checkout: Your Information" step. */
public class CheckoutInformationPage extends BasePage {

    private static final String PATH = "/checkout-step-one.html";

    private final Locator firstNameInput = byTestId("firstName");
    private final Locator lastNameInput = byTestId("lastName");
    private final Locator postalCodeInput = byTestId("postalCode");
    private final Locator continueButton = byTestId("continue");

    public CheckoutInformationPage(Page page) {
        super(page);
    }

    public CheckoutInformationPage waitUntilLoaded() {
        waitForPath(PATH);
        return this;
    }

    public CheckoutOverviewPage continueWith(CheckoutInfo info) {
        firstNameInput.fill(info.firstName());
        lastNameInput.fill(info.lastName());
        postalCodeInput.fill(info.postalCode());
        continueButton.click();
        return new CheckoutOverviewPage(page).waitUntilLoaded();
    }
}
