package org.codegeny.jakartron.mockito;

/*-
 * #%L
 * jakartron-mockito
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

import org.codegeny.jakartron.ProducesLiteral;
import org.junit.platform.commons.annotation.Testable;
import org.kohsuke.MetaInfServices;
import org.mockito.Mock;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

@MetaInfServices
public final class MockitoIntegration implements Extension {

    public void processTestClasses(@Observes @WithAnnotations(Testable.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Mock.class))
                .forEach(f -> f.add(ProducesLiteral.INSTANCE));
    }
}
