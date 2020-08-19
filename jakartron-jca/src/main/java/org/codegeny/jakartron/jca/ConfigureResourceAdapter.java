package org.codegeny.jakartron.jca;

import javax.enterprise.inject.Instance;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import java.util.Properties;

public interface ConfigureResourceAdapter {

    void setResourceAdapter(Class<?> messageListenerInterface, ResourceAdapter resourceAdapter, Class<? extends ActivationSpec> activationSpecClass);

    void addMessageEndpoint(Class<?> messageListenerInterface, Instance<?> messageEndpointProvider, Properties activationProperties, Class<?> endpointClass);

    default void addMessageEndpoint(Class<?> messageListenerInterface, Instance<Object> messageEndpointProvider, Properties activationProperties) {
        addMessageEndpoint(messageListenerInterface, messageEndpointProvider, activationProperties, messageListenerInterface);
    }
}
