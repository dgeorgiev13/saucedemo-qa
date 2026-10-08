package com.saucedemo.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.saucedemo.config.TestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

/**
 * Browser lifecycle shared by all UI tests.
 * <ul>
 *     <li>One {@link Playwright} + {@link Browser} per test class (expensive to start).</li>
 *     <li>A fresh, isolated {@link BrowserContext} + {@link Page} per test (cookies, storage, cart state).</li>
 *     <li>Tracing is always recorded and saved only when the test fails, see {@link FailureArtifactsExtension}.</li>
 * </ul>
 */
@ExtendWith(FailureArtifactsExtension.class)
public abstract class BaseTest {

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute("data-test");
        PlaywrightAssertions.setDefaultAssertionTimeout(TestConfig.defaultTimeoutMs());
        browser = browserType(playwright).launch(new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.headless())
                .setSlowMo(TestConfig.slowMo()));
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext(new Browser.NewContextOptions().setBaseURL(TestConfig.baseUrl()));
        context.setDefaultTimeout(TestConfig.defaultTimeoutMs());
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page = context.newPage();
    }

    /** Called by {@link FailureArtifactsExtension} before {@link #closeContext()} when a test fails. */
    void saveFailureArtifacts(String testName) {
        Path dir = TestConfig.artifactsDir().resolve(testName);
        page.screenshot(new Page.ScreenshotOptions().setPath(dir.resolve("failure.png")).setFullPage(true));
        context.tracing().stop(new Tracing.StopOptions().setPath(dir.resolve("trace.zip")));
        System.out.printf("Failure artifacts saved to %s (open trace.zip at https://trace.playwright.dev)%n",
                dir.toAbsolutePath());
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    private static BrowserType browserType(Playwright playwright) {
        return switch (TestConfig.browser()) {
            case CHROMIUM -> playwright.chromium();
            case FIREFOX -> playwright.firefox();
            case WEBKIT -> playwright.webkit();
        };
    }
}
