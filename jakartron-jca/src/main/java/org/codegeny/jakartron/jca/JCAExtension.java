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
import javax.transaction.TransactionManager;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@MetaInfServices
public class JCAExtension implements Extension {

    private static final class AdapterDefinition {

        private final Class<?> messageListenerInterface;
        private final Class<? extends ResourceAdapter> adapterClass;
        private final Class<? extends ActivationSpec> specClass;
        private final Map<Class<?>, ActivationSpec> listeners = new HashMap<>();

        public AdapterDefinition(Class<?> messageListenerInterface, Class<? extends ResourceAdapter> adapterClass, Class<? extends ActivationSpec> specClass) {
            this.messageListenerInterface = messageListenerInterface;
            this.adapterClass = adapterClass;
            this.specClass = specClass;
        }

        public void start(Instance<Object> instance, BootstrapContext context) throws Exception {
            if (!listeners.isEmpty()) {
                ResourceAdapter adapter = instance.select(adapterClass).get();
                adapter.start(context);
                for (Map.Entry<Class<?>, ActivationSpec> entry : listeners.entrySet()) {
                    entry.getValue().validate();
                    entry.getValue().setResourceAdapter(adapter);
                    MessageEndpointFactory mef = new ProxyMessageEndpointFactory(instance.select(TransactionManager.class).get(), instance, entry.getKey(), messageListenerInterface);
                    adapter.endpointActivation(mef, entry.getValue());
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

        public void add(Class<?> listenerImplementation, Map<String, String> activationConfiguration) {
            try {
                ActivationSpec spec = specClass.newInstance();
                for (Map.Entry<String, String> entry : activationConfiguration.entrySet()) {
                    BeanUtils.copyProperty(spec, entry.getKey(), entry.getValue());
                }
                listeners.put(listenerImplementation, spec);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException exception) {
                throw new RuntimeException("Cannot create ActivationSpec", exception);
            }
        }
    }

    private final Map<Class<?>, AdapterDefinition> adapters = new HashMap<>();

    public void addListener(AnnotatedType<?> listenerImplementation, Class<?> listenerInterface, Map<String, String> activationConfiguration) {
        AdapterDefinition definition = adapters.get(listenerInterface);
        if (definition == null) {
            throw new IllegalStateException("No ResourceAdapter has been registered for " + listenerInterface);
        }
        definition.add(listenerImplementation.getJavaClass().asSubclass(listenerInterface), activationConfiguration);
    }

    public void registerConnector(Class<?> listenerInterface, Class<? extends ResourceAdapter> adapterClass, Class<? extends ActivationSpec> specClass) {
        adapters.put(listenerInterface, new AdapterDefinition(listenerInterface, adapterClass, specClass));
    }

    public void startConnectors(@Observes AfterDeploymentValidation event, BeanManager beanManager) throws Exception {
        Executor threadPool = Executors.newFixedThreadPool(5);
        GeronimoWorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, null);
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, new XATerminatorImple(), new TransactionSynchronizationRegistryImple());
        for (AdapterDefinition definition : adapters.values()) {
            definition.start(beanManager.createInstance(), bootstrapContext);
            beanManager.getExtension(CoreExtension.class).addShutdownHook(() -> definition.stop(beanManager.createInstance()));
        }
    }
}
