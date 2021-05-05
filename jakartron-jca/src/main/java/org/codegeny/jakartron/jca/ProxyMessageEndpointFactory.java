package org.codegeny.jakartron.jca;

/*-
 * #%L
 * jakartron-jca
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

import javax.enterprise.inject.Instance;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class ProxyMessageEndpointFactory<T> implements MessageEndpointFactory {

    public static <T> MessageEndpointFactory of(TransactionManager transactionManager, Instance<T> instance, Class<?> messageListenerInterface, Class<?> endpointClass) {
        return new ProxyMessageEndpointFactory<>(transactionManager, instance, messageListenerInterface, endpointClass);
    }

    private final Class<?> messageListenerInterface;
    private final Class<?> endpointClass;
    private final Instance<T> instance;
    private final TransactionManager transactionManager;

    public ProxyMessageEndpointFactory(TransactionManager transactionManager, Instance<T> instance, Class<?> messageListenerInterface, Class<?> endpointClass) {
        this.transactionManager = transactionManager;
        this.instance = instance;
        this.messageListenerInterface = messageListenerInterface;
        this.endpointClass = endpointClass;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource xaResource, long timeout) {
        return createEndpoint(xaResource);
    }

    @Override
    public String getActivationName() {
        return toString();
    }

    @Override
    public Class<?> getEndpointClass() {
        return endpointClass;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource resource) {
        T endpoint = instance.get();
        MessageEndpoint messageEndpoint = new TransactedMessageEndpoint(transactionManager, resource, () -> instance.destroy(endpoint));
        return (MessageEndpoint) Proxy.newProxyInstance(endpointClass.getClassLoader(), new Class[]{MessageEndpoint.class, messageListenerInterface}, (p, m, a) -> invoke(endpoint, messageEndpoint, m, a));
    }

    @Override
    public boolean isDeliveryTransacted(Method method) {
        return true;
    }

    private static Object invoke(Object delegate, MessageEndpoint messageEndpoint, Method method, Object... args) throws Throwable {
        try {
            return method.invoke(method.getDeclaringClass().equals(MessageEndpoint.class) ? messageEndpoint : delegate, args);
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getTargetException();
        }
    }
}
