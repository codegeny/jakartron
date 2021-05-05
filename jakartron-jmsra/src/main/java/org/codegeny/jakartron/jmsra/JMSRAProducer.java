package org.codegeny.jakartron.jmsra;

/*-
 * #%L
 * jakartron-jmsra
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

import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivationSpec;
import org.codegeny.jakartron.jca.ConfigureResourceAdapter;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.jms.MessageListener;

@Dependent
public class JMSRAProducer {

    public void registerAdapter(@Observes ConfigureResourceAdapter event, ActiveMQServer server) {
        ActiveMQResourceAdapter resourceAdapter = new ActiveMQResourceAdapter();
        resourceAdapter.setConnectorClassName(InVMConnectorFactory.class.getName());
        resourceAdapter.setConnectionParameters(String.format("%s=%s", TransportConstants.SERVER_ID_PROP_NAME, server.getIdentity()));
        event.setResourceAdapter(MessageListener.class, resourceAdapter, ActiveMQActivationSpec.class);
    }
}
