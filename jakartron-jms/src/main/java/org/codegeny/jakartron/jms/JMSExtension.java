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

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQDestination;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.codegeny.jakartron.CoreExtension;
import org.codegeny.jakartron.jndi.JNDI;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@MetaInfServices
public class JMSExtension implements Extension  {

    public static class JMSConnectionFactoryLiteral extends AnnotationLiteral<JMSConnectionFactory> implements JMSConnectionFactory {

        private final String value;

        public JMSConnectionFactoryLiteral(String value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public String value() {
            return value;
        }
    }

    private static final AtomicInteger SERVER_ID = new AtomicInteger();

    private final Set<String> queues = new HashSet<>();

    public void configure(@Observes BeforeBeanDiscovery event) {
        event.configureQualifier(JMSConnectionFactory.class);
    }

    public void processResources(@Observes @WithAnnotations(Resource.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && !f.getAnnotation(Resource.class).lookup().isEmpty() && Queue.class.isAssignableFrom(f.getJavaMember().getType()))
                .forEach(f -> queues.add(f.getAnnotated().getAnnotation(Resource.class).lookup()));
    }

    public void addBeans(@Observes AfterBeanDiscovery event) {
        event.<ActiveMQConnectionFactory>addBean()
                .types(Object.class, ConnectionFactory.class, XAConnectionFactory.class, ActiveMQConnectionFactory.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE, JNDI.Literal.of("connectionFactory"))
                .produceWith(instance -> ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, new TransportConfiguration(InVMConnectorFactory.class.getName(), Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, instance.select(ActiveMQServer.class).get().getIdentity()))))
                .disposeWith((instance, context) -> instance.close());
        event.<JMSContext>addBean()
                .types(Object.class, JMSContext.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .produceWith(instance -> instance.select(ConnectionFactory.class).get().createContext())
                .disposeWith((instance, context) -> instance.close());
        event.<JMSContext>addBean()
                .types(Object.class, JMSContext.class)
                .qualifiers(new JMSConnectionFactoryLiteral("java:/JmsXA"), Any.Literal.INSTANCE)
                .produceWith(instance -> instance.select(XAJMSContext.class).get());
        event.<XAJMSContext>addBean()
                .types(Object.class, XAJMSContext.class)
                .scope(TransactionScoped.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .produceWith(this::xaJMSContext)
                .disposeWith((instance, context) -> instance.close());
        queues.forEach(name -> event.addBean()
                .types(Object.class, Destination.class, Queue.class, ActiveMQDestination.class, ActiveMQQueue.class)
                .scope(ApplicationScoped.class)
                .qualifiers(Any.Literal.INSTANCE, JNDI.Literal.of(name))
                .createWith(context -> ActiveMQDestination.createQueue(name)));
    }

    private XAJMSContext xaJMSContext(Instance<Object> instance) {
        try {
            XAJMSContext context = instance.select(XAConnectionFactory.class).get().createXAContext();
            instance.select(TransactionManager.class).get().getTransaction().enlistResource(context.getXAResource());
            return context;
        } catch (SystemException | RollbackException exception) {
            throw new CreationException(exception);
        }
    }

    public void start(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.<ActiveMQServer>addBean()
                .types(ActiveMQServer.class)
                .scope(Singleton.class)
                .createWith(context -> {
                    String serverId = Integer.toString(SERVER_ID.incrementAndGet());
                    Map<String, Object> params = Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, serverId);
                    Configuration configuration = new ConfigurationImpl()
                            .setSecurityEnabled(false)
                            .setPersistenceEnabled(false)
                            .setJMXManagementEnabled(false)
                            .addAddressesSetting("#", new AddressSettings()
                                    .setAutoCreateQueues(true)
                                    .setDeadLetterAddress(new SimpleString("dlq"))
                                    .setExpiryAddress(new SimpleString("dlq"))
                                    .setMaxDeliveryAttempts(3)
                                    .setRedeliveryDelay(0)
                            )
                            .addQueueConfiguration(new QueueConfiguration("dlq"))
                            .addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName(), params));
                    ActiveMQServer server = ActiveMQServers.newActiveMQServer(configuration);
                    server.setIdentity(serverId);
                    try {
                        server.start();
                    }catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                    beanManager.getExtension(CoreExtension.class).addShutdownHook(server::stop);
                    return server;
                });
   }

   public void addQueue(String name) {
        queues.add(name);
   }
}
