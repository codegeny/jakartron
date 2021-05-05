package org.codegeny.jakartron.servlet;

/*-
 * #%L
 * jakartron-servlet
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
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class SecurityExtension implements Extension {

    private final Set<DeclareUser> users = new HashSet<>();

    public void configureSecurity(@Observes @WithAnnotations({DeclareUser.class, DeclareUsers.class}) ProcessAnnotatedType<?> event) {
        users.addAll(event.getAnnotatedType().getAnnotations(DeclareUser.class));
    }

    public void addObserver(@Observes AfterBeanDiscovery event) {
        event.<SecurityConfigurationEvent>addObserverMethod()
                .observedType(SecurityConfigurationEvent.class)
                .notifyWith(e -> users.forEach(u -> e.getEvent().addUser(u.name(), u.password(), u.roles())));
    }
}
