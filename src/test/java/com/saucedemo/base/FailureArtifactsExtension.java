package com.saucedemo.base;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Saves a screenshot and a Playwright trace when a test fails.
 * {@code afterTestExecution} runs before {@code @AfterEach}, so the browser context is still open.
 */
public class FailureArtifactsExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        boolean failed = extensionContext.getExecutionException().isPresent();
        if (failed && extensionContext.getRequiredTestInstance() instanceof BaseTest test) {
            test.saveFailureArtifacts(extensionContext.getRequiredTestClass().getSimpleName()
                    + "_" + extensionContext.getRequiredTestMethod().getName());
        }
    }
}
