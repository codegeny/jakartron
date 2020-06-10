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
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public final class CDIExtension implements TestInstanceFactory, BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(CDIExtension.class);

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
                .addBeanClasses(context.getRequiredTestClass())
                .addBeanClasses(ReflectionUtils.findNestedClasses(context.getRequiredTestClass(), t -> true).toArray(new Class<?>[0]))
                .initialize()
        );
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        getStore(extensionContext).get(CreationalContext.class, CreationalContext.class).release();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        getStore(extensionContext).put(CreationalContext.class, getBeanManager(extensionContext).createCreationalContext(null));
        getBeanManager(extensionContext).createAnnotatedType(extensionContext.getRequiredTestClass()).getMethods().stream()
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

    private static abstract class AbstractParameterResolver implements ParameterResolver {

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            BeanManager beanManager = getBeanManager(extensionContext);
            Annotation[] qualifiers = qualifiers(beanManager, parameterContext);
            return supportsParameter(beanManager, parameterContext.getParameter().getType(), parameterContext.getParameter().getType(), qualifiers);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            BeanManager beanManager = getBeanManager(extensionContext);
            Annotation[] qualifiers = qualifiers(beanManager, parameterContext);
            return resolveParameter(beanManager, parameterContext.getParameter().getType(), parameterContext.getParameter().getType(), qualifiers);
        }

        protected abstract boolean supportsParameter(BeanManager beanManager, Class<?> rawType, Type genericType, Annotation... qualifiers) throws ParameterResolutionException;

        protected abstract Object resolveParameter(BeanManager beanManager, Class<?> rawType, Type genericType, Annotation... qualifiers) throws ParameterResolutionException;
    }

    private static abstract class SimpleTypeParameterResolver extends AbstractParameterResolver {

        private final Class<?> type;

        SimpleTypeParameterResolver(Class<?> type) {
            this.type = type;
        }

        @Override
        protected boolean supportsParameter(BeanManager beanManager, Class<?> rawType, Type genericType, Annotation... qualifiers) throws ParameterResolutionException {
            return type.equals(rawType);
        }
    }

    public static final class BeanManagerParameterResolver extends SimpleTypeParameterResolver {

        public BeanManagerParameterResolver() {
            super(BeanManager.class);
        }

        @Override
        protected BeanManager resolveParameter(BeanManager beanManager, Class<?> rawType, Type genericType, Annotation... qualifiers) throws ParameterResolutionException {
            return beanManager;
        }
    }

    public static final class InstanceParameterResolver extends SimpleTypeParameterResolver {

        public InstanceParameterResolver() {
            super(Instance.class);
        }

        @Override
        protected Instance<?> resolveParameter(BeanManager beanManager, Class<?> rawType, Type genericType, Annotation... qualifiers) throws ParameterResolutionException {
            return beanManager.createInstance().select(rawType, qualifiers); // Instance does not support select(Type), only select(Class/TypeLiteral).
        }
    }

    public static final class EventParameterResolver extends SimpleTypeParameterResolver {

        public EventParameterResolver() {
            super(Event.class);
        }

        @Override
        protected Event<?> resolveParameter(BeanManager beanManager, Class<?> rawType, Type genericType, Annotation... qualifiers) throws ParameterResolutionException {
            return beanManager.getEvent().select(rawType, qualifiers);
        }
    }

    public static final class BeanParameterResolver implements ParameterResolver {

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            BeanManager beanManager = getBeanManager(extensionContext);
            return beanManager.resolve(beanManager.getBeans(parameterContext.getParameter().getParameterizedType(), qualifiers(beanManager, parameterContext))) != null;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            BeanManager beanManager = getBeanManager(extensionContext);
            AnnotatedMethod<?> annotatedMethod = getStore(extensionContext).get(AnnotatedMethod.class, AnnotatedMethod.class);
            AnnotatedParameter<?> annotatedParameter = annotatedMethod.getParameters().get(parameterContext.getIndex());
            InjectionPoint injectionPoint = beanManager.createInjectionPoint(annotatedParameter);
            return beanManager.getInjectableReference(injectionPoint, getStore(extensionContext).get(CreationalContext.class, CreationalContext.class));
        }
    }
}
