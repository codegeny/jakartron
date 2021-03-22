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
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Jakartron {

    public static SeContainerInitializer initialize(Class<?>... classes) {
        SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        scan(classes, initializer, new HashSet<>());
        return initializer;
    }

    public static <T> void run(Class<T> klass, Consumer<T> application) {
        // TODO what about declared class at level +2?
        try (SeContainer container = initialize(klass).addBeanClasses(klass).addBeanClasses(klass.getDeclaredClasses()).initialize()) {
            application.accept(container.select(klass).get());
        }
    }

    public static <T, R> R call(Class<T> klass, Function<T, R> application) {
        // TODO what about declared class at level +2?
        try (SeContainer container = initialize(klass).addBeanClasses(klass).addBeanClasses(klass.getDeclaredClasses()).initialize()) {
            return application.apply(container.select(klass).get());
        }
    }

    private static void scan(Class<?>[] types, SeContainerInitializer initializer, Set<Class<?>> visited) {
        for (Class<?> type : types) {
            scan(type, initializer, visited);
        }
    }

    @SuppressWarnings("unchecked")
    private static void scan(Class<?> type, SeContainerInitializer initializer, Set<Class<?>> visited) {
        if (type == null || !visited.add(type)) {
            return;
        }
        if (Customizer.class.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers())) {
            try {
                type.asSubclass(Customizer.class).newInstance().customize(initializer);
            } catch (Exception exception) {
                throw new IllegalStateException("Can not instantiate `" + type.getName() + "'", exception);
            }
        } else if (Extension.class.isAssignableFrom(type)) {
            initializer.addExtensions(type.asSubclass(Extension.class));
        } else {
            if (!type.isInterface()) {
                if (type.isAnnotationPresent(Interceptor.class)) {
                    initializer.enableInterceptors(type);
                }
                if (type.isAnnotationPresent(Decorator.class)) {
                    initializer.enableDecorators(type);
                }
                if (type.isAnnotationPresent(Alternative.class)) {
                    initializer.selectAlternatives(type);
                }
            }
            initializer.addBeanClasses(type);
        }
        scan(type.getSuperclass(), initializer, visited);
        scan(type.getInterfaces(), initializer, visited);
        for (Annotation annotation : type.getAnnotations()) {
            scan(annotation, initializer, visited);
        }
    }

    @SuppressWarnings("unchecked")
    private static void scan(Annotation annotation, SeContainerInitializer initializer, Set<Class<?>> visited) {
        if (annotation instanceof AdditionalClasses) {
            AdditionalClasses additionalClasses = (AdditionalClasses) annotation;
            scan(additionalClasses.value(), initializer, visited);
        } else if (annotation instanceof AdditionalPackages) {
            AdditionalPackages additionalPackages = (AdditionalPackages) annotation;
            initializer.addPackages(additionalPackages.recursive(), additionalPackages.value());
        } else if (annotation instanceof EnabledAlternatives) {
            EnabledAlternatives enabledAlternatives = (EnabledAlternatives) annotation;
            for (Class<?> enabledAlternative : enabledAlternatives.value()) {
                if (enabledAlternative.isAnnotation()) {
                    initializer.selectAlternativeStereotypes(enabledAlternative.asSubclass(Annotation.class));
                } else {
                    initializer.selectAlternatives(enabledAlternative);
                }
            }
        } else if (annotation instanceof DisableDiscovery) {
            initializer.disableDiscovery();
        }
    }

    private Jakartron() {
        throw new InternalError();
    }
}
