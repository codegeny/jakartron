package org.codegeny.jakartron.validation;

/*-
 * #%L
 * jakartron-validation
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

import org.codegeny.jakartron.jndi.JNDI;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public final class ValidationProducer {

    @ApplicationScoped
    @Produces
    @Default
    @JNDI("java:comp/ValidatorFactory")
    public ValidatorFactory buildFactory() {
        return Validation.buildDefaultValidatorFactory();
    }

    public void closeFactory(@Disposes ValidatorFactory factory) {
        factory.close();
    }
}
