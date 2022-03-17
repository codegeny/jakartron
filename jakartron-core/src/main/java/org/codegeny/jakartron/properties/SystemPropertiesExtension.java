package org.codegeny.jakartron.properties;

/*-
 * #%L
 * jakartron-core
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

import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.Properties;

@MetaInfServices
public final class SystemPropertiesExtension implements Extension {

    private final Properties properties = new Properties();

    public void backupSystemProperties(@Observes BeforeBeanDiscovery event) {
        properties.putAll(System.getProperties());
    }

    public void setSystemProperties(@Observes @WithAnnotations(SystemProperty.class) ProcessAnnotatedType<?> event) {
        event.getAnnotatedType().getAnnotations(SystemProperty.class)
                .forEach(systemProperty -> System.setProperty(systemProperty.key(), systemProperty.value()));
    }

    public void restoreSystemProperties(@Observes BeforeShutdown event) {
        System.setProperties(properties);
    }
}
