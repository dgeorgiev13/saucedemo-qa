# SauceDemo QA

QA homework for [SauceDemo](https://www.saucedemo.com/): manual test design, exploratory testing and UI automation
with **Playwright for Java**.

| Part | Deliverable |
|---|---|
| 1. Manual testing | [`ManualTestCases.feature`](ManualTestCases.feature) – Gherkin scenarios (positive, negative, edge case) |
| 2. Exploratory testing | [`BugReport.md`](BugReport.md) – one reproducible defect with severity/priority rationale |
| 3. UI automation | [`src/test/java`](src/test/java/com/saucedemo) – Playwright + JUnit 5 + Maven |

## Tech stack

- Java 21 (LTS)
- [Playwright for Java](https://playwright.dev/java/) 1.63
- JUnit 5 (test runner), AssertJ (data assertions)
- Maven via the bundled **Maven Wrapper** – no Maven installation needed

## Prerequisites

- **JDK 21+** with `JAVA_HOME` set (e.g. [Eclipse Temurin](https://adoptium.net/))
- Internet access – on the first run Maven downloads the dependencies and Playwright downloads its browsers
  (to `%LOCALAPPDATA%\ms-playwright` / `~/.cache/ms-playwright`)

## Running the tests

Linux / macOS / Git Bash:

```bash
./mvnw test
```

Windows PowerShell (`-D` arguments must be quoted, otherwise PowerShell splits them at the dot):

```powershell
.\mvnw.cmd test
.\mvnw.cmd test "-Dheadless=false" "-Dslow.mo=500"
```

Common options:

| Option | Default | Example |
|---|---|---|
| `headless` | `true` | `-Dheadless=false` – watch the browser |
| `slow.mo` | `0` | `-Dslow.mo=500` – delay between actions (ms) |
| `browser` | `chromium` | `-Dbrowser=firefox` / `-Dbrowser=webkit` |
| `base.url` | `https://www.saucedemo.com` | `-Dbase.url=https://staging.example.com` |
| `default.timeout.ms` | `10000` | `-Ddefault.timeout.ms=20000` |

Every key in [`config.properties`](src/test/resources/config.properties) can be overridden by a system property
(`-Dbase.url=...`) or an environment variable in UPPER_SNAKE_CASE (`BASE_URL=...`), in that order of precedence.

## Automated scenario

[`PurchaseFlowTest`](src/test/java/com/saucedemo/tests/PurchaseFlowTest.java) implements the assignment step by step
(each step is logged to the console):

1. Resolve the standard user credentials
2. Log in
3. Add the product with the highest and the product with the lowest price
4. Remove the highest-priced product and verify the cart content
5. Sort by **Name (A to Z)** and add the last product
6. Go to checkout
7. Finish the order
8. Verify the order is placed
9. Verify the cart is empty
10. Log out

Additional checks beyond the minimum: the A–Z sorting is really sorted, the checkout *Item total* equals the sum of
the selected product prices, and after logout a protected page is no longer accessible.

## Design decisions

### Credentials are not hard-coded

The assignment states that the credentials are prone to change. [`CredentialsProvider`](src/test/java/com/saucedemo/utils/CredentialsProvider.java)
resolves them in this order:

1. **Explicit override** – `SAUCE_USERNAME` / `SAUCE_PASSWORD` environment variables (or `-Dsauce.username` /
   `-Dsauce.password`). Intended for CI secrets or environments that do not publish credentials.
2. **Read from the application** – SauceDemo publishes the accepted usernames and the shared password on the login
   page. The provider reads them at runtime and selects the user whose name starts with the configured prefix
   (`credentials.standard.user.prefix=standard`). If no or more than one such user exists, the test fails fast
   with a clear message instead of guessing.

The password is never printed – `Credentials.toString()` masks it.

### No hard-coded products

Products, prices and names are read from the UI. The highest/lowest priced products are found with comparators,
prices are parsed into `BigDecimal` (no floating point rounding), and the "last product" is taken from the sorted
list. The test keeps working if the catalogue or prices change.

### Page Object Model

```
src/test/java/com/saucedemo
├── base/        BaseTest (browser lifecycle), FailureArtifactsExtension
├── config/      TestConfig – system property → env var → config.properties
├── model/       Product, Credentials, CheckoutInfo (Java records)
├── pages/       LoginPage, InventoryPage, CartPage, CheckoutInformationPage,
│   │            CheckoutOverviewPage, CheckoutCompletePage, BasePage
│   └── components/  HeaderComponent – cart link/badge, burger menu, logout
├── tests/       PurchaseFlowTest
└── utils/       CredentialsProvider, PriceParser, Steps
src/test/resources
├── config.properties
└── testdata/checkout.properties
```

- Page objects expose **business actions** (`addToCart(product)`, `checkout()`, `logout()`) and return the next page,
  so a test reads like the scenario.
- Locators use the application's `data-test` attributes (`getByTestId`) and ARIA roles – no CSS/XPath tied to layout.
- Assertions are kept in the test; page objects only wait for their page to be loaded.
- Two assertion styles with a clear purpose: Playwright `assertThat` for UI state (auto-retrying, no sleeps) and
  AssertJ `then` for data read from the UI.

### Isolation and stability

- One browser per test class, a **fresh `BrowserContext` per test** – no shared cookies, storage or cart state.
- No `Thread.sleep` / fixed waits – Playwright auto-waiting and web-first assertions only.

### Debugging failed tests

A Playwright trace is recorded for every test and saved, together with a full-page screenshot, **only when the test
fails**:

```
target/test-artifacts/<TestClass>_<testMethod>/
├── failure.png
└── trace.zip
```

Open `trace.zip` at [trace.playwright.dev](https://trace.playwright.dev) (processed locally in the browser) to step
through every action with DOM snapshots, network and console logs.

## Assumptions

- "Standard user" is the published username starting with `standard` (`standard_user` at the time of writing).
- The highest and the lowest priced products are unique; the last product by name is not already in the cart.
  Both are asserted explicitly so a data change produces a clear failure instead of a misleading one.
- The checkout customer data is test data from [`checkout.properties`](src/test/resources/testdata/checkout.properties).
