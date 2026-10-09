package com.saucedemo.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.saucedemo.config.TestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

/**
 * Browser lifecycle for all UI tests: one browser per test class, a fresh isolated context and page per test.
 * A trace is recorded for every test and saved only on failure (see {@link FailureArtifactsExtension}).
 */
@ExtendWith(FailureArtifactsExtension.class)
public abstract class BaseTest {

    private static final Path ARTIFACTS_DIR = Path.of("target", "test-artifacts");

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute("data-test");
        browser = browserType().launch(new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.headless())
                .setSlowMo(TestConfig.slowMo()));
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext();
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
    }

    /** Called on test failure, before {@link #closeContext()}, while the page is still open. */
    void saveFailureArtifacts(String testName) {
        Path dir = ARTIFACTS_DIR.resolve(testName);
        page.screenshot(new Page.ScreenshotOptions().setPath(dir.resolve("failure.png")).setFullPage(true));
        context.tracing().stop(new Tracing.StopOptions().setPath(dir.resolve("trace.zip")));
        System.out.println("Failure artifacts saved to " + dir.toAbsolutePath());
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    private static BrowserType browserType() {
        return switch (TestConfig.browser()) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalStateException("Unsupported browser: " + TestConfig.browser());
        };
    }
}
