package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
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

import org.codegeny.jakartron.PriorityLiteral;
import org.junit.platform.commons.annotation.Testable;
import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class TestExtension implements Extension {

    private final Set<Type> testTypes = new HashSet<>();

    public void processTestClass(@Observes @WithAnnotations(Testable.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType().add(PriorityLiteral.DEFAULT);
        testTypes.add(event.getAnnotatedType().getJavaClass());
    }

    public void processTestAttributes(@Observes ProcessBeanAttributes<?> attributes) {
        if (attributes.getBeanAttributes().getTypes().stream().anyMatch(testTypes::contains)) {
            attributes.configureBeanAttributes()
                    .scope(Singleton.class)
                    .alternative(true);
        }
    }
}
