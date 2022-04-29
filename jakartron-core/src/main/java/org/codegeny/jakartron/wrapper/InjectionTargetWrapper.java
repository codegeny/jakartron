package org.codegeny.jakartron.wrapper;

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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

public abstract class InjectionTargetWrapper<T> extends ProducerWrapper<T> implements InjectionTarget<T> {

    private final InjectionTarget<T> delegate;

    public InjectionTargetWrapper(InjectionTarget<T> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public void inject(T instance, CreationalContext<T> context) {
        delegate.inject(instance, context);
    }

    @Override
    public void postConstruct(T instance) {
        delegate.postConstruct(instance);
    }

    @Override
    public void preDestroy(T instance) {
        delegate.preDestroy(instance);
    }
}
