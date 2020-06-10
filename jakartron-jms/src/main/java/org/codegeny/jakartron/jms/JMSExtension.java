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


import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
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
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQDestination;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivationSpec;
import org.apache.activemq.artemis.utils.Env;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.codegeny.jakartron.jndi.JNDI;
import org.codegeny.jakartron.jta.TransactionalLiteral;
import org.jboss.narayana.jta.jms.TransactionHelper;
import org.jboss.narayana.jta.jms.TransactionHelperImpl;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;
import javax.resource.spi.BootstrapContext;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@MetaInfServices
public class JMSExtension implements Extension {

    private static final AtomicInteger SERVER_ID = new AtomicInteger();

    static {
        Env.setTestEnv(true);
    }

    private ActiveMQServer server;
    private ActiveMQResourceAdapter resourceAdapter;
    private final String serverId = Integer.toString(SERVER_ID.incrementAndGet());
    private final Map<ActiveMQActivationSpec, Class<? extends MessageListener>> messageListeners = new HashMap<>();
    private final Set<String> queues = new HashSet<>();
    private final Set<String> topics = new HashSet<>();

    public void processMessageListeners(@Observes @WithAnnotations(MessageDriven.class) ProcessAnnotatedType<? extends MessageListener> event) throws Exception {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && MessageDrivenContext.class.isAssignableFrom(f.getJavaMember().getType()))
                .forEach(f -> f.add(InjectLiteral.INSTANCE));

        event.configureAnnotatedType()
                .add(new TransactionalLiteral(Transactional.TxType.REQUIRED));

        ActiveMQActivationSpec activationSpec = new ActiveMQActivationSpec();
        for (ActivationConfigProperty property : event.getAnnotatedType().getAnnotation(MessageDriven.class).activationConfig()) {
            BeanUtils.copyProperty(activationSpec, property.propertyName(), property.propertyValue());
        }
        if (Topic.class.getName().equals(activationSpec.getDestinationType())) {
            topics.add(activationSpec.getDestination());
        } else {
            queues.add(activationSpec.getDestination());
            activationSpec.setDestinationType(Queue.class.getName());
        }
        messageListeners.put(activationSpec, event.getAnnotatedType().getJavaClass());
    }

    public void addBeans(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .types(TransactionHelper.class)
                .produceWith(instance -> new TransactionHelperImpl(instance.select(TransactionManager.class).get()));
        event.<ActiveMQConnectionFactory>addBean()
                .types(Object.class, ConnectionFactory.class, XAConnectionFactory.class, ActiveMQConnectionFactory.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE, JNDI.Literal.of("connectionFactory"))
                .createWith(context -> ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, new TransportConfiguration(InVMConnectorFactory.class.getName(), Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, serverId))))
                .disposeWith((instance, context) -> instance.close());
        event.<JMSContext>addBean()
                .types(Object.class, JMSContext.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .produceWith(instance -> instance.select(ConnectionFactory.class).get().createContext())
                .disposeWith((instance, context) -> instance.close());
        event.<JMSContext>addBean()
                .types(Object.class, JMSContext.class)
                .qualifiers(XA.Literal.INSTANCE, Any.Literal.INSTANCE)
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
        topics.forEach(name -> event.addBean()
                .types(Object.class, Destination.class, Topic.class, ActiveMQDestination.class, ActiveMQTopic.class)
                .scope(ApplicationScoped.class)
                .qualifiers(Any.Literal.INSTANCE, JNDI.Literal.of(name))
                .createWith(context -> ActiveMQDestination.createTopic(name)));
        event.addBean()
                .qualifiers(Any.Literal.INSTANCE)
                .types(Object.class, MessageDrivenContext.class)
                .produceWith(MyMessageDrivenContext::new);
    }

    private XAJMSContext xaJMSContext(Instance<Object> instance) {
        try {
            XAJMSContext context = instance.select(XAConnectionFactory.class).get().createXAContext();
            instance.select(TransactionHelper.class).get().registerXAResource(context.getXAResource());
            return context;
        } catch (JMSException jmsException) {
            throw new JMSRuntimeException(jmsException.getMessage(), jmsException.getErrorCode(), jmsException.getCause());
        }
    }

    public void start(@Observes AfterDeploymentValidation event, BeanManager beanManager) throws Exception {
        Map<String, Object> params = Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, serverId);
        Configuration configuration = new ConfigurationImpl()
                .setSecurityEnabled(false)
                .setPersistenceEnabled(false)
                .setJMXManagementEnabled(false)
                .addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName(), params));

        server = ActiveMQServers.newActiveMQServer(configuration);
        server.start();

        Executor threadPool = Executors.newFixedThreadPool(5);
        GeronimoWorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, null);
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, new XATerminatorImple(), new TransactionSynchronizationRegistryImple());

        resourceAdapter = new ActiveMQResourceAdapter();
        resourceAdapter.setConnectorClassName(InVMConnectorFactory.class.getName());
        resourceAdapter.setConnectionParameters(String.format("%s=%s", TransportConstants.SERVER_ID_PROP_NAME, serverId));
        resourceAdapter.start(bootstrapContext);

        for (Map.Entry<ActiveMQActivationSpec, Class<? extends MessageListener>> entry : messageListeners.entrySet()) {
            entry.getKey().validate();
            entry.getKey().setResourceAdapter(resourceAdapter);
            resourceAdapter.endpointActivation(new MessageListenerEndpointFactory(beanManager.createInstance().select(entry.getValue()), entry.getValue().getName()), entry.getKey());
        }
    }

    public void stop(@Observes BeforeShutdown event) throws Exception {
        try {
            resourceAdapter.stop();
        } finally {
            server.stop();
        }
    }
}
