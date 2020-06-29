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

import javax.enterprise.inject.Instance;
import javax.jms.MessageListener;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

public class JMSMessageEndpointFactory implements MessageEndpointFactory {

    private final String name;
    private final Instance<? extends MessageListener> instance;
    private final TransactionManager transactionManager;

    public JMSMessageEndpointFactory(TransactionManager transactionManager, Instance<? extends MessageListener> instance, String name) {
        this.transactionManager = transactionManager;
        this.instance = instance;
        this.name = name;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource xaResource, long timeout) {
        return createEndpoint(xaResource);
    }

    @Override
    public String getActivationName() {
        return name;
    }

    @Override
    public Class<?> getEndpointClass() {
        return MessageListener.class;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource resource) {
        return new JMSMessageEndpoint<>(transactionManager, resource, instance);
    }

    @Override
    public boolean isDeliveryTransacted(Method method) {
        return true;
    }
}
