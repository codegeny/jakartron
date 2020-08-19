package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import org.codegeny.jakartron.jca.ConfigureResourceAdapter;
import org.codegeny.jakartron.jta.TransactionalLiteral;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import javax.transaction.Transactional;
import java.io.Externalizable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MetaInfServices
public final class EJBIntegration implements Extension {

    public void common(@Observes @WithAnnotations({Stateless.class, Singleton.class, Stateful.class, MessageDriven.class}) ProcessAnnotatedType<?> event) {

        TransactionManagementType transactionManagementType = event.getAnnotatedType().isAnnotationPresent(TransactionManagement.class)
                ? event.getAnnotatedType().getAnnotation(TransactionManagement.class).value()
                : TransactionManagementType.CONTAINER;

        AnnotatedTypeConfigurator<?> configurator = event.configureAnnotatedType()
                .add(() -> ActivateRequestContext.class);

        configurator
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && EJBContext.class.isAssignableFrom(f.getJavaMember().getType()))
                .forEach(f -> f.add(InjectLiteral.INSTANCE));

        if (transactionManagementType == TransactionManagementType.CONTAINER) {
            event.configureAnnotatedType()
                    .add(transactionLiteral(event.getAnnotatedType()))
                    .filterMethods(m -> m.isAnnotationPresent(TransactionAttribute.class))
                    .forEach(m -> m.add(transactionLiteral(m.getAnnotated())));
        }
    }

    public void scope(@Observes @WithAnnotations({Stateless.class, Singleton.class}) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType().add(ApplicationScoped.Literal.INSTANCE);
    }

    private final Set<AnnotatedType<?>> messageDrivenBeans = new HashSet<>();

    public void processMessageListeners(@Observes @WithAnnotations(MessageDriven.class) ProcessAnnotatedType<?> event, BeanManager beanManager) {
        messageDrivenBeans.add(event.getAnnotatedType());
    }

    public void registerMDB(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.<ConfigureResourceAdapter>addObserverMethod()
                .observedType(ConfigureResourceAdapter.class)
                .notifyWith(e -> messageDrivenBeans.forEach(mdb -> {
                    MessageDriven messageDriven = mdb.getAnnotation(MessageDriven.class);
                    Class<?> listenerInterface;
                    if (messageDriven.messageListenerInterface().equals(Object.class)) {
                        Set<Class<?>> interfaces = getAllInterfaces(mdb.getJavaClass())
                                .distinct()
                                .filter(i -> !i.getPackage().getName().startsWith("javax.ejb"))
                                .filter(i -> !Arrays.asList(Serializable.class, Externalizable.class).contains(i))
                                .collect(Collectors.toSet());
                        if (interfaces.size() != 1) {
                            throw new RuntimeException("MDB must implement a single interface or specify @MessageDriven.listenerInterface()");
                        }
                        listenerInterface = interfaces.iterator().next();
                    } else {
                        listenerInterface = messageDriven.messageListenerInterface();
                    }

                    Properties properties = new Properties();
                    Stream.of(messageDriven.activationConfig()).forEach(a -> properties.setProperty(a.propertyName(), a.propertyValue()));

                    e.getEvent().addMessageEndpoint(listenerInterface, beanManager.createInstance().select(mdb.getJavaClass()), properties, mdb.getJavaClass());
                }));
    }

    private static Stream<Class<?>> getAllInterfaces(Class<?> klass) {
        return klass == null ? Stream.empty() : Stream.<Stream<Class<?>>>of(
                klass.isInterface() ? Stream.of(klass) : Stream.empty(),
                getAllInterfaces(klass.getSuperclass()),
                Stream.of(klass.getInterfaces()).flatMap(EJBIntegration::getAllInterfaces)
        ).flatMap(Function.identity());
    }

    private static TransactionalLiteral transactionLiteral(Annotated annotated) {
        return new TransactionalLiteral(annotated.isAnnotationPresent(TransactionAttribute.class)
                ? Transactional.TxType.valueOf(annotated.getAnnotation(TransactionAttribute.class).value().name())
                : Transactional.TxType.REQUIRED
        );
    }
}
