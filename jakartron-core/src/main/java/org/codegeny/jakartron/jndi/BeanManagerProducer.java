package org.codegeny.jakartron.jndi;

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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;

@Dependent
public class BeanManagerProducer {

    public static final String BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";

    @Produces
    @JNDI(BEAN_MANAGER_JNDI_NAME)
    public BeanManager beanManager(BeanManager beanManager) {
        return beanManager;
    }
}
