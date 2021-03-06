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
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.kohsuke.MetaInfServices;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.interceptor.Interceptor;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class MockitoIntegration implements Extension {

    @Priority(Interceptor.Priority.PLATFORM_BEFORE - 1000)
    public static class MockitoBean {}

    private final Set<BeanContract> mocks = new HashSet<>();
    private final Set<BeanContract> spies = new HashSet<>();

    public void process(@Observes @WithAnnotations(Testable.class) ProcessAnnotatedType<?> event, BeanManager beanManager) {
        event.getAnnotatedType().getMethods().stream()
                .filter(m -> m.isAnnotationPresent(Test.class))
                .forEach(m -> m.getParameters().forEach(p -> process(beanManager.createInjectionPoint(p), beanManager)));
        event.getAnnotatedType().getFields()
                .forEach(f -> process(beanManager.createInjectionPoint(f), beanManager));
    }

    private void process(InjectionPoint injectionPoint, BeanManager beanManager) {
        if (injectionPoint.getAnnotated().isAnnotationPresent(Mock.class)) {
            mocks.add(new BeanContract(injectionPoint.getType(), injectionPoint.getQualifiers(), beanManager));
        }
        if (injectionPoint.getAnnotated().isAnnotationPresent(Spy.class)) {
            spies.add(new BeanContract(injectionPoint.getType(), injectionPoint.getQualifiers(), beanManager));
        }
    }

    public void addAlternative(@Observes AfterTypeDiscovery event) {
        event.getAlternatives().add(MockitoBean.class);
    }

    public void registerBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        mocks.forEach(contract -> event.addBean()
                .beanClass(MockitoBean.class)
                .alternative(true)
                .types(contract.getType())
                .qualifiers(contract.getQualifiers())
                .scope(TestScoped.class)
                .createWith(context -> Mockito.mock((Class<?>) contract.getType()))
        );
        spies.forEach(contract -> event.addBean()
                .beanClass(MockitoBean.class)
                .alternative(true)
                .types(contract.getType())
                .qualifiers(contract.getQualifiers())
                .scope(TestScoped.class)
                .createWith(context -> {
                    Set<Bean<?>> beans = new HashSet<>(beanManager.getBeans(contract.getType(), contract.getQualifiers().toArray(new Annotation[0])));
                    beans.removeIf(bean -> MockitoBean.class.equals(bean.getBeanClass()));
                    Bean<?> bean = beanManager.resolve(beans);
                    return Mockito.spy(beanManager.getReference(bean, contract.getType(), beanManager.createCreationalContext(bean)));
                })
        );
    }
}
