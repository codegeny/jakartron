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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class JMSProducer {

    private final static AtomicInteger SERVER_ID = new AtomicInteger();

    private final Map<String, Object> params = Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, Integer.toString(SERVER_ID.incrementAndGet()));

    private ActiveMQServer server;

    @PostConstruct
    public void startServer() {
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
        System.out.println("server started");
    }

    @PreDestroy
    public void stopServer() {
        try {
            server.stop();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }
        System.out.println("server stopped");
    }

    @Produces
    @ApplicationScoped
    public ActiveMQConnectionFactory createConnectionFactory() {
        System.out.println("creating connectionfactory");
        return ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, new TransportConfiguration(InVMConnectorFactory.class.getName(), params)).disableFinalizeChecks();
    }

    public void closeConnectionFactory(@Disposes ActiveMQConnectionFactory connectionFactory) {
        System.out.println("closing connectionfactory");
        connectionFactory.close();
    }

    @Produces
    @ApplicationScoped
    public JMSContext createJMSContext(ConnectionFactory connectionFactory) {
        System.out.println("creating jmsContext");
        return connectionFactory.createContext();
    }

    public void closeJMSContext(@Disposes JMSContext jmsContext) {
        System.out.println("closing jmsContext");
        jmsContext.close();
    }

//    public void startServer(@Observes @Initialized(ApplicationScoped.class) Object event, ActiveMQServer server) throws Exception {
//        server.start();
//    }
//
//    public void stopServer(@Observes @Destroyed(ApplicationScoped.class) Object event, ActiveMQServer server) throws Exception {
//        server.stop();
//    }
}
