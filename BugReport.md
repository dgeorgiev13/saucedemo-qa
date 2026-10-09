# Bug Report

## BUG-001: Order can be placed with an empty cart

| Field | Value |
|---|---|
| **Component** | Cart / Checkout |
| **Environment** | https://www.saucedemo.com · Google Chrome 153.0.8010.53 · Windows 11 Pro |
| **User** | `standard_user` |
| **Reproducibility** | Always (3/3 manual attempts, also reproduced with Playwright) |
| **Severity** | Major |
| **Priority** | High |
| **Date found** | 2026-10-09 |

### Summary

The **Checkout** button stays enabled when the cart is empty. The user can go through the whole checkout flow
without any products and gets the order confirmation *"Thank you for your order!"* for an order with no items
and a total of $0.00.

### Preconditions

- The user is logged in as `standard_user`.
- The cart is empty (no badge on the cart icon).

### Steps to reproduce

1. Log in as `standard_user`.
2. Without adding any product, click the cart icon in the top-right corner.
3. On the **Your Cart** page (no items listed), click **Checkout**.
4. Fill in *First Name* `John`, *Last Name* `Doe`, *Zip/Postal Code* `1000` and click **Continue**.
5. On **Checkout: Overview** click **Finish**.

### Expected result

Checkout is not possible with an empty cart. For example, the **Checkout** button is disabled or hidden, or
clicking it shows a message such as *"Your cart is empty"*, and the user stays on the cart page. No order is
created.

### Actual result

- Step 3: the user is taken to **Checkout: Your Information** (`/checkout-step-one.html`).
- Step 4: **Checkout: Overview** shows no items, `Item total: $0`, `Tax: $0.00`, `Total: $0.00`.
- Step 5: **Checkout: Complete!** (`/checkout-complete.html`) shows *"Thank you for your order!"* and
  *"Your order has been dispatched…"*.

### Evidence

| Step | Screenshot |
|---|---|
| Empty cart with an enabled *Checkout* button | [bug1-1-empty-cart.png](screenshots/bug1-1-empty-cart.png) |
| Overview with no items and $0.00 total | [bug1-2-empty-overview.png](screenshots/bug1-2-empty-overview.png) |
| Order confirmation | [bug1-3-order-complete.png](screenshots/bug1-3-order-complete.png) |

No errors in the browser console.

### Severity rationale: Major

- It breaks a core business rule of the main flow: an order must contain at least one product.
- In a real shop each such order would create an invalid order record and trigger the downstream steps (payment
  authorisation for $0, fulfilment, confirmation e-mail). This creates noise and manual clean-up for operations.
- The customer gets a confirmation for an order with no products, which is misleading.
- It is **not Critical**: no data loss, no security impact, and the normal purchase flow still works.

### Priority rationale: High

- It is in the checkout flow, which every customer uses and which directly affects revenue and data quality.
- It is cheap to fix and low-risk: disable the *Checkout* button when the cart is empty in the UI, **and** reject
  an empty order on the server side, because the UI check alone can be bypassed by opening
  `/checkout-step-one.html` directly.
- It should be fixed in the current or next sprint. It does not need a hotfix, because valid orders are
  unaffected.

### Suggested regression test

Automate: an empty cart → the *Checkout* button is disabled, and opening `/checkout-step-one.html` directly
with an empty cart does not lead to an order confirmation.

---

## Additional observations (out of scope of the single reported bug)

These came up during the same exploratory session. They are less severe than BUG-001.

### OBS-1: Item total is shown with a floating-point precision error. Severity Minor, Priority Medium.

`standard_user`, Chrome 153. Add **Sauce Labs Onesie** ($7.99) and **Sauce Labs Fleece Jacket** ($49.99), then go
to **Checkout: Overview**. It shows `Item total: $57.980000000000004` instead of `$57.98`. `Tax` and `Total`
($62.62) are correct, so only the displayed value is wrong. The amount is not formatted to 2 decimals. The same
cause shows `Item total: $0` instead of `$0.00` for an empty cart (see BUG-001).
Evidence: [obs1-item-total-precision.jpg](screenshots/obs1-item-total-precision.jpg)

### OBS-2: QTY in the cart looks editable but is read-only. Usability issue, Severity Minor, Priority Low.

On **Your Cart** the quantity is shown in a bordered box that looks like an input field, but it cannot be
changed. The application does not support quantities at all (each product can be added once). There are two
possible fixes, and the Product Owner decides which: style QTY as plain text (UI fix), or implement quantity
editing (new feature).
Evidence: [obs2-qty-looks-editable.jpg](screenshots/obs2-qty-looks-editable.jpg)
