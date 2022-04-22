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
import org.codegeny.jakartron.junit.TestEvent;
import org.codegeny.jakartron.junit.TestPhase;
import org.codegeny.jakartron.junit.TestScoped;
import org.kohsuke.MetaInfServices;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@MetaInfServices
public final class MockitoIntegration implements Extension {

    private final Set<BeanContract> mocks = new HashSet<>();
    private final Set<BeanContract> spies = new HashSet<>();

    public void process(@Observes ProcessInjectionPoint<?, ?> event, BeanManager beanManager) {
        if (event.getInjectionPoint().getAnnotated().isAnnotationPresent(Mock.class)) {
            mocks.add(new BeanContract(event.getInjectionPoint(), beanManager));
        }
        if (event.getInjectionPoint().getAnnotated().isAnnotationPresent(Spy.class)) {
            spies.add(new BeanContract(event.getInjectionPoint(), beanManager));
        }
    }

    public void addAlternative(@Observes AfterTypeDiscovery event) {
        event.getAlternatives().add(getClass());
    }

    public void registerBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        Set<Object> reset = new HashSet<>();
        mocks.forEach(contract -> event.addBean()
                .alternative(true)
                .types(contract.getType())
                .qualifiers(contract.getQualifiers())
                .scope(Singleton.class)
                .createWith(context -> {
                    Object mock = Mockito.mock((Class<?>) contract.getType());
                    reset.add(mock);
                    return mock;
                })
        );
        spies.forEach(contract -> event.addBean()
                .alternative(true)
                .types(contract.getType())
                .qualifiers(contract.getQualifiers())
                .scope(Singleton.class)
                .createWith(context -> {
                    Bean<?> bean = beanManager.getBeans(contract.getType(), contract.getQualifiersAsArray()).stream()
                            .filter(b -> !getClass().equals(b.getBeanClass()))
                            .collect(Collectors.collectingAndThen(Collectors.toSet(), beanManager::resolve));
                    Object spy = Mockito.spy(beanManager.getReference(bean, contract.getType(), context));
                    reset.add(spy);
                    return spy;
                })
        );
        event.addObserverMethod()
                .observedType(Object.class)
                .qualifiers(TestEvent.Literal.of(TestPhase.AFTER_EACH))
                .notifyWith(e -> reset.forEach(Mockito::reset));
    }
}
