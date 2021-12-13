package org.codegeny.jakartron;

/*-
 * #%L
 * jakartron-core
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

import org.codegeny.jakartron.concurrent.ConcurrenceProducer;
import org.codegeny.jakartron.jndi.JNDIExtension;
import org.codegeny.jakartron.logging.LoggerProducer;

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
import java.util.stream.Stream;

public final class Jakartron {

    public static SeContainerInitializer initialize(Class<?>... classes) {
        return initialize(Stream.of(classes));
    }

    public static SeContainerInitializer initialize(Stream<Class<?>> classes) {
        SeContainerInitializer initializer = SeContainerInitializer.newInstance()
                .addExtensions(CoreExtension.class, JNDIExtension.class)
                .addBeanClasses(ConcurrenceProducer.class, LoggerProducer.class);
        Set<Class<?>> visited = new HashSet<>();
        classes.forEach(c -> scanAnnotations(c, initializer, visited));
        return initializer;
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

        if (Customizer.class.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers())) {
            try {
                type.asSubclass(Customizer.class).newInstance().customize(initializer);
            } catch (Exception exception) {
                throw new IllegalStateException("Can not instantiate `" + type.getName() + "'", exception);
            }
        } else if (Extension.class.isAssignableFrom(type)) {
            initializer.addExtensions(type.asSubclass(Extension.class));
        } else {
            if (type.isAnnotationPresent(Interceptor.class)) {
                initializer.enableInterceptors(type);
            }
            if (type.isAnnotationPresent(Decorator.class)) {
                initializer.enableDecorators(type);
            }
            if (type.isAnnotationPresent(Alternative.class)) {
                if (!type.isAnnotation()) {
                    initializer.selectAlternatives(type);
                }
            }
            initializer.addBeanClasses(type);
        }

        AdditionalClasses additionalClasses = type.getAnnotation(AdditionalClasses.class);
        if (additionalClasses != null) {
            for (Class<?> additionalClass : additionalClasses.value()) {
                scanAnnotations(additionalClass, initializer, visited);
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
