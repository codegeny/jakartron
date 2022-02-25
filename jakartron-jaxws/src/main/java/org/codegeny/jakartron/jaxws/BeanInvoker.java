package org.codegeny.jakartron.jaxws;

/*-
 * #%L
 * jakartron-jaxws
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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class BeanInvoker extends Invoker {

    private final BeanManager beanManager;
    private Bean<?> bean;

    BeanInvoker(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public Object invoke(Packet packet, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        CreationalContext<?> context = beanManager.createCreationalContext(null);
        try {
            return method.invoke(beanManager.getReference(bean, Object.class, context), args);
        } finally {
            context.release();
        }
    }

    @Override
    public void start(WSWebServiceContext webServiceContext, WSEndpoint endpoint) {
        bean = beanManager.resolve(beanManager.getBeans(endpoint.getImplementationClass()));
    }
}
