package org.codegeny.jakartron.jta;

/*-
 * #%L
 * jakartron-jta
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

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.transaction.Synchronization;

final class DestroyingSynchronization<T> implements Synchronization {

    private final T value;
    private final Contextual<T> contextual;
    private final CreationalContext<T> creationalContext;

    DestroyingSynchronization(T value, Contextual<T> contextual, CreationalContext<T> creationalContext) {
        this.value = value;
        this.contextual = contextual;
        this.creationalContext = creationalContext;
    }

    @Override
    public void beforeCompletion() {
    }

    @Override
    public void afterCompletion(int status) {
        contextual.destroy(value, creationalContext);
    }
}
