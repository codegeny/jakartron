package org.codegeny.jakartron;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.extension.ExtensionRegistry;

import java.util.logging.Logger;

public class LifecycleExtension implements
        AfterAllCallback,
        AfterEachCallback,
        AfterEachMethodAdapter,
        AfterTestExecutionCallback,
        BeforeAllCallback,
        BeforeEachCallback,
        BeforeEachMethodAdapter,
        BeforeTestExecutionCallback,
        InvocationInterceptor,
        LifecycleMethodExecutionExceptionHandler,
        TestExecutionExceptionHandler,
        TestInstancePostProcessor,
        TestInstancePreDestroyCallback {

    private static final Logger LOGGER = Logger.getLogger(LifecycleExtension.class.getName());

    @Override
    public void afterAll(ExtensionContext context) {
        LOGGER.info("afterAll " + context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        LOGGER.info("afterEach " + context.getDisplayName());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        LOGGER.info("afterTestExecution " + context.getDisplayName());
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        LOGGER.info("beforeAll " + context.getDisplayName());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        LOGGER.info("beforeEach " + context.getDisplayName());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        LOGGER.info("beforeTestExecution " + context.getDisplayName());
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        LOGGER.info("handleTestExecutionException " + context.getDisplayName());
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        LOGGER.info("postProcessTestInstance " + context.getDisplayName());
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) {
        LOGGER.info("preDestroyTestInstance " + context.getDisplayName());
    }

    @Override
    public void invokeAfterEachMethod(ExtensionContext context, ExtensionRegistry registry) throws Throwable {
        LOGGER.info("invokeAfterEachMethod " + context.getDisplayName());
    }

    @Override
    public void invokeBeforeEachMethod(ExtensionContext context, ExtensionRegistry registry) throws Throwable {
        LOGGER.info("invokeBeforeEachMethod " + context.getDisplayName());
    }
}