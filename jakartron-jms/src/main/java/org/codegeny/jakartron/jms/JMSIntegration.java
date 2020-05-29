package org.codegeny.jakartron.jms;

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

import org.codegeny.jakartron.ObservesAsyncLiteral;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.*;
import javax.jms.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

// TODO support MessageDrivenContext
@MetaInfServices
public final class JMSIntegration implements Extension {

    private final Set<String> names = new HashSet<>();

    public void makeObserver(@Observes @WithAnnotations(MessageDriven.class) ProcessAnnotatedType<? extends MessageListener> event) {
        Stream.of(event.getAnnotatedType().getAnnotation(MessageDriven.class).activationConfig())
                .filter(ac -> ac.propertyName().equals("destination"))
                .map(ActivationConfigProperty::propertyValue)
                .findAny()
                .ifPresent(name -> event.configureAnnotatedType()
                        .remove(MessageDriven.class::isInstance)
                        .add(ApplicationScoped.Literal.INSTANCE)
                        .filterMethods(m -> m.getJavaMember().getName().equals("onMessage") && m.getParameters().size() == 1 && m.getParameters().get(0).getJavaParameter().getType().equals(Message.class))
                        .forEach(m -> m.params().forEach(p -> p.add(new ObservesAsyncLiteral()).add(NamedLiteral.of(name))))
                );
    }

    public void processResources(@Observes @WithAnnotations(Resource.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && Destination.class.isAssignableFrom(f.getJavaMember().getType()))
                .forEach(f -> {
                    String name = f.getAnnotated().getAnnotation(Resource.class).name();
                    if (name.isEmpty()) {
                        name = f.getAnnotated().getJavaMember().getName();
                    }
                    names.add(name);
                    f.add(InjectLiteral.INSTANCE).add(NamedLiteral.of(name));
                });
    }

    public void createDestinations(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.<JMSEvent>addObserverMethod()
                .observedType(JMSEvent.class)
                .transactionPhase(TransactionPhase.AFTER_SUCCESS)
                .notifyWith(e -> e.getEvent().fire(beanManager.getEvent().select(Message.class)));
        names.forEach(name -> event.addBean()
                .createWith(c -> new DestinationImpl(name))
                .scope(ApplicationScoped.class)
                .types(Object.class, Destination.class, Queue.class, Topic.class)
                .qualifiers(Any.Literal.INSTANCE, NamedLiteral.of(name))
        );
    }
}
