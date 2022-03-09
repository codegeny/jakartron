package org.codegeny.jakartron.mockito;

/*-
 * #%L
 * jakartron-mockito
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

import org.codegeny.jakartron.BeanContract;
import org.codegeny.jakartron.junit.TestScoped;
import org.mockito.Mockito;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.interceptor.Interceptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * This extension will add a mock for each non-resolvable injection point.
 * <p>
 * This extension is opt-in (not listed in META-INF/services/javax.enterprise.inject.spi.Extension).
 */
public final class AutoMockExtension implements Extension {

    private final Set<BeanContract> contracts = new HashSet<>();

    public <T, X> void inspectInjectionPoint(@Observes ProcessInjectionPoint<T, X> event, BeanManager beanManager) {
        contracts.add(new BeanContract(event.getInjectionPoint(), beanManager));
    }

    public void createMocks(@Observes @Priority(Interceptor.Priority.LIBRARY_AFTER) AfterBeanDiscovery event, BeanManager beanManager) {
        // create a mock for all non-resolvable injection point
        contracts.stream()
                .filter(contract -> beanManager.resolve(beanManager.getBeans(contract.getType(), contract.getQualifiersAsArray())) == null)
                .forEach(contract -> event.addBean()
                        .scope(TestScoped.class)
                        .qualifiers(contract.getQualifiers())
                        .types(contract.getType())
                        .produceWith(instance -> Mockito.mock(rawType(contract.getType())))
                );
    }

    private static Class<?> rawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return rawType(((ParameterizedType) type).getRawType());
        }
        throw new CreationException("Cannot mock " + type);
    }
}
