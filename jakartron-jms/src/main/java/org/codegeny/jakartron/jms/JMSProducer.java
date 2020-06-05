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
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
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
import org.apache.activemq.artemis.utils.Env;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JMSProducer {

    private static final Logger LOGGER = Logger.getLogger(JMSProducer.class.getName());
    private final static AtomicInteger SERVER_ID = new AtomicInteger();

    private final Map<String, Object> params = Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, Integer.toString(SERVER_ID.incrementAndGet()));

    private ActiveMQServer server;
    private ActiveMQConnectionFactory connectionFactory;
//    private JMSContext context;

    @PostConstruct
    public void startServer() {
        Env.setTestEnv(true);
        Configuration configuration = new ConfigurationImpl()
                .setSecurityEnabled(false)
                .setPersistenceEnabled(false)
                .setJMXManagementEnabled(false)
                .addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName(), params));
        server = ActiveMQServers.newActiveMQServer(configuration);
        try {
            server.start();
        } catch (Exception exception) {
            throw new RuntimeException();
        }
        connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, new TransportConfiguration(InVMConnectorFactory.class.getName(), params)).disableFinalizeChecks();
//        context = connectionFactory.createContext();
    }

    @PreDestroy
    public void stopServer() {
        try {
//            context.close();
        } finally {
            try {
                connectionFactory.close();
            } finally {
                try {
                    server.stop();
                } catch (Exception exception) {
                    LOGGER.log(Level.WARNING, "Error while shutting down JMS", exception);
                }
            }
        }
    }

    @Produces
    @ApplicationScoped
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Produces
    @RequestScoped
    public JMSContext jmsContext() {
        return connectionFactory.createContext();
    }

    public void close(@Disposes JMSContext context) {
        context.close();
    }
}
