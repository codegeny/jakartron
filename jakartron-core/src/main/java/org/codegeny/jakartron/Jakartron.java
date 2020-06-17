package org.codegeny.jakartron;

/*-
 * #%L
 * jakartron-core
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

import javax.decorator.Decorator;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.Extension;
import javax.interceptor.Interceptor;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Jakartron {

    public static SeContainerInitializer initialize(Class<?> klass) {
        return scanAnnotations(klass, SeContainerInitializer.newInstance(), new HashSet<>());
    }

    public static <T> void run(Class<T> klass, Consumer<T> application) {
        try (SeContainer container = initialize(klass).addBeanClasses(klass).addBeanClasses(klass.getDeclaredClasses()).initialize()) {
            application.accept(container.select(klass).get());
        }
    }

    public static <T, R> R call(Class<T> klass, Function<T, R> application) {
        try (SeContainer container = initialize(klass).addBeanClasses(klass).addBeanClasses(klass.getDeclaredClasses()).initialize()) {
            return application.apply(container.select(klass).get());
        }
    }

    @SuppressWarnings("unchecked")
    private static SeContainerInitializer scanAnnotations(Class<?> type, SeContainerInitializer initializer, Set<Class<?>> visited) {
        if (type == null || !visited.add(type)) {
            return initializer;
        }

        AdditionalClasses additionalClasses = type.getAnnotation(AdditionalClasses.class);
        if (additionalClasses != null) {
            for (Class<?> additionalClass : additionalClasses.value()) {
                if (Customizer.class.isAssignableFrom(additionalClass)) {
                    try {
                        additionalClass.asSubclass(Customizer.class).newInstance().customize(initializer);
                    } catch (Exception exception) {
                        throw new IllegalStateException("Can not instantiate `" + type.getName() + "'", exception);
                    }
                } else if (Extension.class.isAssignableFrom(additionalClass)) {
                    initializer.addExtensions(additionalClass.asSubclass(Extension.class));
                } else {
                    if (additionalClass.isAnnotationPresent(Interceptor.class)) {
                        initializer.enableInterceptors(additionalClass);
                    }
                    if (additionalClass.isAnnotationPresent(Decorator.class)) {
                        initializer.enableDecorators(additionalClass);
                    }
                    if (additionalClass.isAnnotationPresent(Alternative.class)) {
                        initializer.selectAlternatives(additionalClass);
                    }
                    initializer.addBeanClasses(additionalClass);
                }
            }
        }

        AdditionalPackages additionalPackages = type.getAnnotation(AdditionalPackages.class);
        if (additionalPackages != null) {
            initializer.addPackages(additionalPackages.recursive(), additionalPackages.value());
        }

        EnabledAlternatives enabledAlternatives = type.getAnnotation(EnabledAlternatives.class);
        if (enabledAlternatives != null) {
            for (Class<?> enabledAlternative : enabledAlternatives.value()) {
                if (enabledAlternative.isAnnotation()) {
                    initializer.selectAlternativeStereotypes(enabledAlternative.asSubclass(Annotation.class));
                } else {
                    initializer.selectAlternatives(enabledAlternative);
                }
            }
        }

        if (type.isAnnotationPresent(DisableDiscovery.class)) {
            initializer.disableDiscovery();
        }

        return Stream.<Stream<Class<?>>>of(
                Stream.of(type.getSuperclass()),
                Stream.of(type.getInterfaces()),
                Stream.of(type.getAnnotations()).map(Annotation::annotationType)
        ).flatMap(Function.identity()).reduce(initializer, (i, t) -> scanAnnotations(t, i, visited), (a, b) -> null);
    }

    private Jakartron() {
        throw new InternalError();
    }
}
