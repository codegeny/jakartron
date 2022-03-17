package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
 * %%
 * Copyright (C) 2018 - 2021 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.codegeny.jakartron.Jakartron;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.util.ReflectionUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public final class JakartronExtension implements TestInstanceFactory, BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver, TestInstancePreDestroyCallback {

    private static final Namespace NAMESPACE = Namespace.create(JakartronExtension.class);

    @Override
    public void afterAll(ExtensionContext context) {
        getBeanManager(context).fireEvent(context, TestEvent.Literal.of(TestPhase.AFTER_ALL));
        SeContainer container = getStore(context).get(SeContainer.class, SeContainer.class);
        container.close();
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        SeContainer container = Jakartron.initialize(Stream.concat(Stream.of(extensionContext.getRequiredTestClass()), ReflectionUtils.findNestedClasses(extensionContext.getRequiredTestClass(), t -> true).stream()))
                .addExtensions(new TestExtension())
                .addBeanClasses(extensionContext.getRequiredTestClass())
                .initialize();
        getStore(extensionContext).put(SeContainer.class, container);
        getStore(extensionContext).put(AnnotatedType.class, container.getBeanManager().createAnnotatedType(extensionContext.getRequiredTestClass()));
        container.getBeanManager().fireEvent(extensionContext, TestEvent.Literal.of(TestPhase.BEFORE_ALL));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        getBeanManager(extensionContext).fireEvent(extensionContext, TestEvent.Literal.of(TestPhase.AFTER_EACH));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        BeanManager beanManager = getBeanManager(extensionContext);
        AnnotatedType<?> annotatedType = getStore(extensionContext).get(AnnotatedType.class, AnnotatedType.class);
        annotatedType.getMethods().stream()
                .filter(m -> m.getJavaMember().equals(extensionContext.getRequiredTestMethod()))
                .findFirst()
                .ifPresent(m -> getStore(extensionContext).put(AnnotatedMethod.class, m));
        beanManager.fireEvent(extensionContext, TestEvent.Literal.of(TestPhase.BEFORE_EACH));
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext testInstanceFactoryContext, ExtensionContext extensionContext) {
        BeanManager beanManager = getBeanManager(extensionContext);
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
        getStore(extensionContext).put(CreationalContext.class, creationalContext);
        Bean<?> testBean = beanManager.resolve(beanManager.getBeans(extensionContext.getRequiredTestClass()));
        return beanManager.getReference(testBean, extensionContext.getRequiredTestClass(), creationalContext);
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext extensionContext) {
        getBeanManager(extensionContext).getExtension(TestExtension.class).reset();
        getStore(extensionContext).get(CreationalContext.class, CreationalContext.class).release();
    }

    public static BeanManager getBeanManager(ExtensionContext extensionContext) {
        return getStore(extensionContext).get(SeContainer.class, SeContainer.class).getBeanManager();
    }

    public static ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        BeanManager beanManager = getBeanManager(extensionContext);
        InjectionPoint injectionPoint = injectionPoint(parameterContext, extensionContext, beanManager);
        return beanManager.resolve(beanManager.getBeans(injectionPoint.getType(), injectionPoint.getQualifiers().toArray(new Annotation[0]))) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        BeanManager beanManager = getBeanManager(extensionContext);
        InjectionPoint injectionPoint = injectionPoint(parameterContext, extensionContext, beanManager);
        return beanManager.getInjectableReference(injectionPoint, getStore(extensionContext).get(CreationalContext.class, CreationalContext.class));
    }

    private static InjectionPoint injectionPoint(ParameterContext parameterContext, ExtensionContext extensionContext, BeanManager beanManager) {
        AnnotatedMethod<?> annotatedMethod = getStore(extensionContext).get(AnnotatedMethod.class, AnnotatedMethod.class);
        AnnotatedParameter<?> annotatedParameter = annotatedMethod.getParameters().get(parameterContext.getIndex());
        return beanManager.createInjectionPoint(annotatedParameter);
    }
}
