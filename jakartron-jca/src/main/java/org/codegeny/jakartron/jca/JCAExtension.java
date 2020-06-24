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
import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@MetaInfServices
public class JCAExtension implements Extension {

    private static final class AdapterDefinition<L, S extends ActivationSpec> {

        private final Class<? extends ResourceAdapter> adapterClass;
        private final Supplier<S> activationSpecSupplier;
        private final Consumer<S> activationSpecConsumer;
        private final Function<Class<? extends L>, MessageEndpointFactory> messageEndpointFactoryBuilder;
        private final Map<Class<? extends L>, ActivationSpec> listeners = new HashMap<>();

        public AdapterDefinition(Class<? extends ResourceAdapter> adapterClass, Supplier<S> activationSpecSupplier, Consumer<S> activationSpecConsumer, Function<Class<? extends L>, MessageEndpointFactory> messageEndpointFactoryBuilder) {
            this.adapterClass = adapterClass;
            this.activationSpecSupplier = activationSpecSupplier;
            this.activationSpecConsumer = activationSpecConsumer;
            this.messageEndpointFactoryBuilder = messageEndpointFactoryBuilder;
        }

        public void start(Instance<Object> instance, BootstrapContext context) throws Exception {
            if (!listeners.isEmpty()) {
                ResourceAdapter adapter = instance.select(adapterClass).get();
                adapter.start(context);
                for (Map.Entry<Class<? extends L>, ActivationSpec> entry : listeners.entrySet()) {
                    entry.getValue().validate();
                    entry.getValue().setResourceAdapter(adapter);
                    adapter.endpointActivation(messageEndpointFactoryBuilder.apply(entry.getKey()), entry.getValue());
                }
            }
        }

        public void stop(Instance<Object> instance) {
            if (!listeners.isEmpty()) {
                ResourceAdapter adapter = instance.select(adapterClass).get();
//                for (Map.Entry<Class<?>, ActivationSpec> entry : listeners.entrySet()) {
//                    adapter.endpointDeactivation(messageEndpointFactoryBuilder.apply(entry.getKey()), entry.getValue());
//                }
                adapter.stop();
            }
        }

        public void add(Class<? extends L> listenerImplementation, Map<String, String> activationConfiguration) {
            S spec = activationSpecSupplier.get();
            try {
                for (Map.Entry<String, String> entry : activationConfiguration.entrySet()) {
                    BeanUtils.copyProperty(spec, entry.getKey(), entry.getValue());
                }
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException("Cannot create ActivationSpec", exception);
            }
            activationSpecConsumer.accept(spec);
            listeners.put(listenerImplementation, spec);
        }
    }

    private final Map<Class<?>, AdapterDefinition<?, ?>> adapters = new HashMap<>();

    public <L> void addListener(AnnotatedType<?> listenerImplementation, Class<L> listenerInterface, Map<String, String> activationConfiguration) {
        AdapterDefinition<L, ?> definition = (AdapterDefinition<L, ?>) adapters.get(listenerInterface);
        if (definition == null) {
            throw new IllegalStateException("No ResourceAdapter has been registered for " + listenerInterface);
        }
        definition.add(listenerImplementation.getJavaClass().asSubclass(listenerInterface), activationConfiguration);
    }

    public <L, S extends ActivationSpec> void registerConnector(Class<L> listenerInterface, Class<? extends ResourceAdapter> adapterClass, Supplier<S> activationSpecSupplier, Consumer<S> activationSpecConsumer, Function<Class<? extends L>, MessageEndpointFactory> messageEndpointFactoryBuilder) {
        adapters.put(listenerInterface, new AdapterDefinition<>(adapterClass, activationSpecSupplier, activationSpecConsumer, messageEndpointFactoryBuilder));
    }

    public void startConnectors(@Observes AfterDeploymentValidation event, BeanManager beanManager) throws Exception {
        Executor threadPool = Executors.newFixedThreadPool(5);
        GeronimoWorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, null);
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, new XATerminatorImple(), new TransactionSynchronizationRegistryImple());
        for (AdapterDefinition<?, ?> definition : adapters.values()) {
            definition.start(beanManager.createInstance(), bootstrapContext);
            beanManager.getExtension(CoreExtension.class).addShutdownHook(() -> definition.stop(beanManager.createInstance()));
        }
    }
}
