package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-jms
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

import org.codegeny.jakartron.jca.JCAExtension;
import org.codegeny.jakartron.jta.TransactionalLiteral;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.transaction.Transactional;
import java.io.Externalizable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MetaInfServices
public class MDBExtension implements Extension {

    private final Set<Class<?>> messageDrivenBeans = new HashSet<>();

    public void processMessageListeners(@Observes @WithAnnotations(MessageDriven.class) ProcessAnnotatedType<?> event, BeanManager beanManager) throws Exception {

        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && MessageDrivenContext.class.isAssignableFrom(f.getJavaMember().getType()))
                .forEach(f -> f.add(InjectLiteral.INSTANCE));

        event.configureAnnotatedType()
                .add(new TransactionalLiteral(Transactional.TxType.REQUIRED));

        MessageDriven messageDriven = event.getAnnotatedType().getAnnotation(MessageDriven.class);

        Class<?> listenerInterface;
        if (messageDriven.messageListenerInterface().equals(Object.class)) {
            Set<Class<?>> interfaces = getAllInterfaces(event.getAnnotatedType().getJavaClass())
                    .distinct()
                    .filter(i -> !i.getPackage().getName().startsWith("javax.ejb"))
                    .filter(i -> !Arrays.asList(Serializable.class, Externalizable.class).contains(i))
                    .collect(Collectors.toSet());
            if (interfaces.size() != 1) {
                throw new RuntimeException("MDB must implements a single interface or specify @MessageDriven.listenerInterface()");
            }
            listenerInterface = interfaces.iterator().next();
        } else {
            listenerInterface = messageDriven.messageListenerInterface();
        }

        Map<String, String> activationConfiguration = Stream.of(messageDriven.activationConfig())
                .collect(Collectors.toMap(ActivationConfigProperty::propertyName, ActivationConfigProperty::propertyValue));

        beanManager.getExtension(JCAExtension.class).addListener(event.getAnnotatedType(), listenerInterface, activationConfiguration);
    }

    private static Stream<Class<?>> getAllInterfaces(Class<?> klass) {
        return klass == null ? Stream.empty() : Stream.<Stream<Class<?>>> of(
                klass.isInterface() ? Stream.of(klass) : Stream.empty(),
                getAllInterfaces(klass.getSuperclass()),
                Stream.of(klass.getInterfaces()).flatMap(MDBExtension::getAllInterfaces)
        ).flatMap(Function.identity());
    }
}
