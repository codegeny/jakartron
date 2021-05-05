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
