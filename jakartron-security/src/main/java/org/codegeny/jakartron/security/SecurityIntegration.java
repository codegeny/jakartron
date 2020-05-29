package org.codegeny.jakartron.security;
/*-
 * #%L
 * jakartron-security
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

import org.kohsuke.MetaInfServices;

import javax.annotation.security.RunAs;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.Nonbinding;

@MetaInfServices
public class SecurityIntegration implements Extension {

    public void configureBinding(@Observes BeforeBeanDiscovery event) {
        event.configureInterceptorBinding(RunAs.class).methods().forEach(c -> c.add(Nonbinding.Literal.INSTANCE));
    }
}
