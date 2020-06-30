package org.codegeny.jakartron.jca;

/*-
 * #%L
 * jakartron-jca
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
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class ProxyMessageEndpointFactory implements MessageEndpointFactory {

    private final Class<?> endpointClass;
    private final Class<?> messageListenerInterface;
    private final Instance<Object> endpointInstance;
    private final TransactionManager transactionManager;

    public ProxyMessageEndpointFactory(TransactionManager transactionManager, Instance<Object> endpointInstance, Class<?> endpointClass, Class<?> messageListenerInterface) {
        this.transactionManager = transactionManager;
        this.endpointInstance = endpointInstance;
        this.endpointClass = endpointClass;
        this.messageListenerInterface = messageListenerInterface;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource xaResource, long timeout) {
        return createEndpoint(xaResource);
    }

    @Override
    public String getActivationName() {
        return endpointClass.getName();
    }

    @Override
    public Class<?> getEndpointClass() {
        return endpointClass;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource resource) {
        return createEndpoint(resource, endpointClass);
    }

    @Override
    public boolean isDeliveryTransacted(Method method) {
        return true;
    }

    private <T> MessageEndpoint createEndpoint(XAResource resource, Class<T> endpointClass) {
        Instance<T> instance = endpointInstance.select(endpointClass);
        T delegate = instance.get();
        MessageEndpoint messageEndpoint = new TransactedMessageEndpoint(transactionManager, resource) {

            @Override
            public void release() {
                endpointInstance.destroy(delegate);
            }
        };
        return (MessageEndpoint) Proxy.newProxyInstance(
                endpointClass.getClassLoader(),
                new Class[] { MessageEndpoint.class, messageListenerInterface},
                (proxy, method, args) -> {
                    try {
                        return method.invoke(method.getDeclaringClass().equals(MessageEndpoint.class) ? messageEndpoint : delegate, args);
                    } catch (InvocationTargetException invocationTargetException) {
                        throw invocationTargetException.getTargetException();
                    }
                }
        );
    }
}
