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

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.codegeny.jakartron.CoreExtension;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.TransactionManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@ApplicationScoped
public class JCAManager {

    public static class Adapter {

        private final Class<?> messageListenerInterface;
        private ResourceAdapter resourceAdapter;
        private Class<? extends ActivationSpec> activationSpecClass;
        private final Set<Consumer<TransactionManager>> activations = new HashSet<>();

        public Adapter(Class<?> messageListenerInterface) {
            this.messageListenerInterface = messageListenerInterface;
        }

        public void setResourceAdapter(ResourceAdapter resourceAdapter, Class<? extends ActivationSpec> activationSpecClass) {
            this.resourceAdapter = resourceAdapter;
            this.activationSpecClass = activationSpecClass;
        }

        public void addMessageEndpoint(Instance<?> messageEndpointProvider, Properties properties, Class<?> endpointClass) {
            activations.add(tm -> {
                try {
                    ActivationSpec spec = activationSpecClass.newInstance();
                    for (String key : properties.stringPropertyNames()) {
                        BeanUtils.copyProperty(spec, key, properties.get(key));
                    }
                    spec.setResourceAdapter(resourceAdapter);
                    resourceAdapter.endpointActivation(ProxyMessageEndpointFactory.of(tm, messageEndpointProvider, messageListenerInterface, endpointClass), spec);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
        }

        public Optional<ResourceAdapter> configure(TransactionManager tm) {
            if (activations.isEmpty() || resourceAdapter == null) {
                return Optional.empty();
            }
            activations.forEach(c -> c.accept(tm));
            return Optional.of(resourceAdapter);
        }
    }

    @Inject
    private Event<ConfigureResourceAdapter> configureEvent;

    private final Map<Class<?>, Adapter> adapters = new ConcurrentHashMap<>();

    public void start(@Observes @Initialized(ApplicationScoped.class) Object event, TransactionManager tm, BeanManager bm) throws Exception {
        configureEvent.fire(new ConfigureResourceAdapter() {

            @Override
            public void setResourceAdapter(Class<?> messageListenerInterface, ResourceAdapter resourceAdapter, Class<? extends ActivationSpec> activationSpecClass) {
                adapters.computeIfAbsent(messageListenerInterface, Adapter::new).setResourceAdapter(resourceAdapter, activationSpecClass);
            }

            @Override
            public void addMessageEndpoint(Class<?> messageListenerInterface, Instance<?> messageEndpointProvider, Properties properties, Class<?> endpointClass) {
                adapters.computeIfAbsent(messageListenerInterface, Adapter::new).addMessageEndpoint(messageEndpointProvider, properties, endpointClass);
            }
        });
        Executor threadPool = Executors.newFixedThreadPool(5);
        GeronimoWorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, null);
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, new XATerminatorImple(), new TransactionSynchronizationRegistryImple());
        for (Adapter adapter : adapters.values()) {
            adapter.resourceAdapter.start(bootstrapContext);
            bm.getExtension(CoreExtension.class).addShutdownHook(adapter.resourceAdapter::stop);
            Optional<ResourceAdapter> a = adapter.configure(tm);
            if (a.isPresent()) {
                ResourceAdapter ra = a.get();
            }
        }
    }
}
