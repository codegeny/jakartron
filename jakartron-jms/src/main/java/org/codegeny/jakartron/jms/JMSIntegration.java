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

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// TODO support MessageDrivenContext
@MetaInfServices
public final class JMSIntegration implements Extension {

    private final Set<String> names = new HashSet<>();
    private final Map<String, Class<? extends MessageListener>> messageDrivenBeans = new HashMap<>();

    public void makeObserver(@Observes @WithAnnotations(MessageDriven.class) ProcessAnnotatedType<? extends MessageListener> event) {
        Stream.of(event.getAnnotatedType().getAnnotation(MessageDriven.class).activationConfig())
                .filter(ac -> ac.propertyName().equals("destination"))
                .map(ActivationConfigProperty::propertyValue)
                .findAny()
                .ifPresent(name -> {
                    names.add(name);
                    messageDrivenBeans.put(name, event.getAnnotatedType().getJavaClass());
                });
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
        names.forEach(name -> event.addBean()
                .createWith(c -> beanManager.createInstance().select(JMSContext.class).get().createQueue(name))
                .scope(ApplicationScoped.class)
                .types(Object.class, Destination.class, Queue.class, ActiveMQQueue.class)
                .qualifiers(Any.Literal.INSTANCE, NamedLiteral.of(name))
        );
    }

    public void initializeMessageDrivenBeans(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        messageDrivenBeans.forEach((name, klass) -> {
            Queue queue = beanManager.createInstance().select(Queue.class, NamedLiteral.of(name)).get();
            MessageListener listener = beanManager.createInstance().select(klass).get();
            JMSContext context = beanManager.createInstance().select(JMSContext.class).get();
            context.createConsumer(queue).setMessageListener(listener);
        });
    }
}
