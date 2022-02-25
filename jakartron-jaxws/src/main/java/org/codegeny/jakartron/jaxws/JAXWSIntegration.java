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

import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.jws.WebService;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class JAXWSIntegration implements Extension {

    private final Set<Class<?>> implementorClasses = new HashSet<>();

    public void collectImplementorClass(@Observes @WithAnnotations(WebService.class) ProcessAnnotatedType<?> event) {
        implementorClasses.add(event.getAnnotatedType().getJavaClass());
    }

    public Set<Class<?>> getImplementorClasses() {
        return implementorClasses;
    }
}
