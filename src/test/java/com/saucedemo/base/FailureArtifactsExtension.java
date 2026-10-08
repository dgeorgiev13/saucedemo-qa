package com.saucedemo.base;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Saves a screenshot and a Playwright trace when a test fails.
 * <p>
 * {@code afterTestExecution} runs right after the test method and before {@code @AfterEach},
 * so the browser context is still open and the trace can be written.
 */
public class FailureArtifactsExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        if (extensionContext.getExecutionException().isEmpty()) {
            return;
        }
        if (extensionContext.getRequiredTestInstance() instanceof BaseTest test) {
            String testName = extensionContext.getRequiredTestClass().getSimpleName()
                    + "_" + extensionContext.getRequiredTestMethod().getName();
            try {
                test.saveFailureArtifacts(testName);
            } catch (RuntimeException e) {
                // Never hide the original test failure because of a reporting problem.
                System.err.println("Could not save failure artifacts: " + e.getMessage());
            }
        }
    }
}
