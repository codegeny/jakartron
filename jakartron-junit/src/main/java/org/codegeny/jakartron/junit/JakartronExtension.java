package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
 * %%
 * Copyright (C) 2018 - 2020 Codegeny
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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.platform.commons.util.ReflectionUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public final class JakartronExtension implements TestInstanceFactory, BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(JakartronExtension.class);

    @Override
    public void afterAll(ExtensionContext context) {
        SeContainer container = getStore(context).get(SeContainer.class, SeContainer.class);
        if (container != null) {
            container.close();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        getStore(context).put(SeContainer.class, Jakartron.initialize(context.getRequiredTestClass())
                .addExtensions(new TestExtension())
                .addBeanClasses(context.getRequiredTestClass())
                .addBeanClasses(ReflectionUtils.findNestedClasses(context.getRequiredTestClass(), t -> true).toArray(new Class<?>[0]))
                .initialize()
        );
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        getStore(extensionContext).get(CreationalContext.class, CreationalContext.class).release();
        BeanManager beanManager = getBeanManager(extensionContext);
        beanManager.getExtension(TestExtension.class).reset();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        BeanManager beanManager = getBeanManager(extensionContext);
        getStore(extensionContext).put(CreationalContext.class, getBeanManager(extensionContext).createCreationalContext(null));
        beanManager.createAnnotatedType(extensionContext.getRequiredTestClass()).getMethods().stream()
                .filter(m -> m.getJavaMember().equals(extensionContext.getRequiredTestMethod()))
                .findFirst()
                .ifPresent(m -> getStore(extensionContext).put(AnnotatedMethod.class, m));
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext testInstanceFactoryContext, ExtensionContext extensionContext) {
        return getBeanManager(extensionContext).createInstance().select(extensionContext.getRequiredTestClass()).get();
    }

    public static BeanManager getBeanManager(ExtensionContext extensionContext) {
        return getStore(extensionContext).get(SeContainer.class, SeContainer.class).getBeanManager();
    }

    public static ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE);
    }

    private static Annotation[] qualifiers(BeanManager beanManager, ParameterContext parameterContext) {
        return Stream.of(parameterContext.getParameter().getAnnotations()).filter(a -> beanManager.isQualifier(a.annotationType())).toArray(Annotation[]::new);
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

    private InjectionPoint injectionPoint(ParameterContext parameterContext, ExtensionContext extensionContext, BeanManager beanManager) {
        AnnotatedMethod<?> annotatedMethod = getStore(extensionContext).get(AnnotatedMethod.class, AnnotatedMethod.class);
        AnnotatedParameter<?> annotatedParameter = annotatedMethod.getParameters().get(parameterContext.getIndex());
        return beanManager.createInjectionPoint(annotatedParameter);
    }
}
