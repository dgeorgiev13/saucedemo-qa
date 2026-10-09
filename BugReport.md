# Bug Report

Three defects found during an exploratory session of the purchase flow with `standard_user`.
**BUG-1 is the main reported defect** requested by the assignment; BUG-2 and BUG-3 are lower-severity
findings from the same session.

## BUG-1: Order can be placed with an empty cart

| Field | Value |
|---|---|
| **Component** | Cart / Checkout |
| **Environment** | https://www.saucedemo.com · Google Chrome 153.0.8010.53 · Windows 11 Pro |
| **User** | `standard_user` |
| **Reproducibility** | Always (3/3 manual attempts, also reproduced with Playwright) |
| **Severity** | Major |
| **Priority** | High |

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
4. Fill in *First Name* `Daniel`, *Last Name* `Georgiev`, *Zip/Postal Code* `1000` and click **Continue**.
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

Checkout Overview with no items, `Total: $0.00` and an enabled *Finish* button:
[Bug1.jpg](screenshots/Bug1.jpg)

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

## BUG-2: Item total is displayed with a floating-point precision error

| Field | Value |
|---|---|
| **Component** | Checkout Overview |
| **Environment** | https://www.saucedemo.com · Google Chrome 153.0.8010.53 · Windows 11 Pro |
| **User** | `standard_user` |
| **Reproducibility** | Always |
| **Severity** | Minor |
| **Priority** | Medium |

### Steps to reproduce

1. Log in as `standard_user`.
2. Add **Sauce Labs Onesie** ($7.99) and **Sauce Labs Fleece Jacket** ($49.99) to the cart.
3. Open the cart, click **Checkout**, fill in the form and click **Continue**.

### Expected result

`Item total: $57.98`, formatted to 2 decimals like `Tax` and `Total`.

### Actual result

`Item total: $57.980000000000004`. `Tax: $4.64` and `Total: $62.62` are correct, so only the displayed
value is wrong. The same cause shows `Item total: $0` instead of `$0.00` for an empty cart (see BUG-1).

### Evidence

[Bug2.jpg](screenshots/Bug2.jpg)

### Severity / priority rationale

- **Minor:** the calculation is correct and the order can be completed; only the display is wrong.
- **Medium:** prices are visible on the checkout page to every customer and the error looks unprofessional
  and reduces trust right before payment. The fix is trivial (format the amount to 2 decimals).

---

## BUG-3: QTY in the cart is styled as an editable field but is read-only

| Field | Value |
|---|---|
| **Component** | Cart |
| **Environment** | https://www.saucedemo.com · Google Chrome 153.0.8010.53 · Windows 11 Pro |
| **User** | `standard_user` |
| **Reproducibility** | Always |
| **Type** | Usability |
| **Severity** | Minor |
| **Priority** | Low |

### Steps to reproduce

1. Log in as `standard_user`.
2. Add any two products to the cart, e.g. **Sauce Labs Bike Light** and **Sauce Labs Backpack**.
3. Open the cart and click the quantity box (`1`) next to a product.

### Expected result

Either the quantity can be changed, or it is shown as plain text that does not look editable.

### Actual result

The quantity is shown in a bordered box that looks like an input field, but clicking it does nothing and the
value cannot be changed.

### Evidence

[Bug3.jpg](screenshots/Bug3.jpg)

### Severity / priority rationale

- **Minor:** no functional impact; the user can still complete the purchase.
- **Low:** cosmetic; it does not block the purchase.
- The application does not support quantities at all (each product can be added once), so there are two
  possible fixes and the Product Owner decides which: style QTY as plain text (UI fix) or implement
  quantity editing (new feature).
