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

    private final Map<Contextual<?>, Instance<?>> map = new ConcurrentHashMap<>();

    @Override
    public Class<TestScoped> getScope() {
        return TestScoped.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> context) {
        return (T) map.computeIfAbsent(contextual, c -> Instance.of(contextual, context)).get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual) {
        return (T) map.getOrDefault(contextual, Instance.NULL).get();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        map.computeIfPresent(contextual, (key, instance) -> instance.destroy());
    }

    public void reset() {
        map.values().forEach(Instance::destroy);
        map.clear();
    }

    private static final class Instance<T> {

        static <T> Instance<T> of(Contextual<T> contextual, CreationalContext<T> context) {
            T instance = contextual.create(context);
            return new Instance<>(instance, () -> contextual.destroy(instance, context));
        }

        static final Instance<?> NULL = new Instance<>(null, () -> {
        });

        private final T instance;
        private final Runnable destroyer;

        Instance(T instance, Runnable destroyer) {
            this.instance = instance;
            this.destroyer = destroyer;
        }

        Instance<?> destroy() {
            destroyer.run();
            return null;
        }

        T get() {
            return this.instance;
        }
    }
}
