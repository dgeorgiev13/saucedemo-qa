# SauceDemo QA

QA homework for [SauceDemo](https://www.saucedemo.com/): manual test design, exploratory testing and UI automation
with **Playwright for Java**.

| Part | Deliverable |
|---|---|
| 1. Manual testing | [`ManualTestCases.feature`](ManualTestCases.feature) – Gherkin scenarios (positive, negative, edge case) |
| 2. Exploratory testing | [`BugReport.md`](BugReport.md) – one reproducible defect with severity/priority rationale |
| 3. UI automation | [`src/test/java`](src/test/java/com/saucedemo) – Playwright + JUnit 5 + Maven |

## Tech stack

Java 21 · Playwright for Java · JUnit 5 · AssertJ · Maven (via the bundled Maven Wrapper – no Maven installation needed)

## Running the tests

Prerequisite: **JDK 21+** with `JAVA_HOME` set. On the first run Maven downloads the dependencies and Playwright
downloads its browsers.

```bash
./mvnw test                                        # Linux / macOS / Git Bash
./mvnw test -Dheadless=false -Dslow.mo=500         # watch the browser
```

```powershell
.\mvnw.cmd test                                    # Windows PowerShell
.\mvnw.cmd test "-Dheadless=false" "-Dslow.mo=500" # quote -D arguments in PowerShell
```

Configuration lives in [`config.properties`](src/test/resources/config.properties) (`base.url`, `browser`,
`headless`, `slow.mo`). Any key can be overridden by a system property (`-Dbrowser=firefox`) or an environment
variable (`BROWSER=firefox`).

## Project structure

```
src/test/java/com/saucedemo
├── base/        BaseTest – browser lifecycle; FailureArtifactsExtension – trace + screenshot on failure
├── config/      TestConfig – system property → environment variable → config.properties
├── model/       Product, Credentials, CheckoutInfo
├── pages/       LoginPage, InventoryPage, CartPage, CheckoutInformationPage,
│   │            CheckoutOverviewPage, CheckoutCompletePage, BasePage
│   └── components/  HeaderComponent – cart and burger menu, shared by several pages
├── tests/       PurchaseFlowTest – the automated scenario
└── utils/       CredentialsProvider
src/test/resources
├── config.properties
└── testdata/checkout.properties
```

## Design decisions

**Page Object Model.** Each page encapsulates its locators and exposes business actions (`addToCart(product)`,
`checkout()`, `logout()`) that return the next page, so the test reads like the scenario. A page object waits for
its URL when it is created. The header (cart, menu) is a reusable component instead of being duplicated per page.

**Locators.** Only the application's `data-test` attributes (`getByTestId`) and ARIA roles – no CSS/XPath tied to
layout.

**Credentials are not hard-coded.** [`CredentialsProvider`](src/test/java/com/saucedemo/utils/CredentialsProvider.java)
uses `SAUCE_USERNAME` / `SAUCE_PASSWORD` when they are set (e.g. CI secrets). Otherwise it reads the accepted
usernames and the shared password from the login page at runtime and picks the user starting with `standard`.
If there is no such user, or more than one, the test fails with a clear message instead of guessing.

**No hard-coded products.** Names and prices are read from the UI. Prices are `BigDecimal`. The highest and lowest
priced products are found with comparators, and the "last product" is taken from the list after sorting.

**No fixed waits.** No `Thread.sleep` – Playwright auto-waiting and web-first assertions only. Playwright
`assertThat` is used for UI state and AssertJ `assertThat` for data read from the UI.

**Isolation.** One browser per test class, a fresh `BrowserContext` (cookies, storage, cart) per test.

**Diagnostics.** A Playwright trace is recorded for every test and, together with a screenshot, saved to
`target/test-artifacts/<test>/` only when the test fails. Open `trace.zip` at
[trace.playwright.dev](https://trace.playwright.dev) to replay every step with DOM snapshots.
