package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
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

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TestContext implements AlterableContext {

    private final Map<Contextual<?>, BeanInstance<?>> map = new ConcurrentHashMap<>();

    @Override
    public Class<TestScoped> getScope() {
        return TestScoped.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> context) {
        return (T) map.computeIfAbsent(contextual, c -> new BeanInstance<T>(contextual, context)).get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual) {
        BeanInstance<?> instance = map.get(contextual);
        return instance == null ? null : (T) instance.get();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        map.computeIfPresent(contextual, (key, instance) -> {
            instance.destroy();
            return null;
        });
    }

    public void reset() {
        map.values().forEach(BeanInstance::destroy);
        map.clear();
    }

    private static final class BeanInstance<T> {

        // Lazy creating the instance because I had deadlocks in the map.computeIfAbsent()
        private volatile T instance;
        private final Contextual<T> contextual;
        private final CreationalContext<T> context;

        BeanInstance(Contextual<T> contextual, CreationalContext<T> context) {
            this.contextual = contextual;
            this.context = context;
        }

        synchronized void destroy() {
            if (instance != null) {
                contextual.destroy(instance, context);
            }
        }

        synchronized T get() {
            if (instance == null) {
                instance = contextual.create(context);
            }
            return this.instance;
        }
    }
}
