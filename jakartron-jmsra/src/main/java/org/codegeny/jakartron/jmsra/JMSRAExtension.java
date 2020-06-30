package org.codegeny.jakartron.jmsra;

/*-
 * #%L
 * jakartron-jmsra
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

import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivationSpec;
import org.codegeny.jakartron.jca.JCAExtension;
import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Singleton;
import javax.jms.MessageListener;

@MetaInfServices
public class JMSRAExtension implements Extension {

    public void initialize(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        beanManager.getExtension(JCAExtension.class).registerConnector(MessageListener.class, ActiveMQResourceAdapter.class, ActiveMQActivationSpec.class);
    }

    public void addBeans(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .types(ActiveMQResourceAdapter.class)
                .scope(Singleton.class)
                .produceWith(instance -> {
                    String serverId = instance.select(ActiveMQServer.class).get().getIdentity();
                    ActiveMQResourceAdapter resourceAdapter = new ActiveMQResourceAdapter();
                    resourceAdapter.setConnectorClassName(InVMConnectorFactory.class.getName());
                    resourceAdapter.setConnectionParameters(String.format("%s=%s", TransportConstants.SERVER_ID_PROP_NAME, serverId));
                    return resourceAdapter;
                });
    }
}
