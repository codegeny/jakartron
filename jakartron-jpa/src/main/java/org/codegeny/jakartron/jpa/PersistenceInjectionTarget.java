package org.codegeny.jakartron.jpa;

/*-
 * #%L
 * jakartron-jpa
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

import org.codegeny.jakartron.wrapper.InjectionTargetWrapper;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.lang.annotation.Annotation;
import java.util.Set;

final class PersistenceInjectionTarget<T> extends InjectionTargetWrapper<T> {

    private final BeanManager beanManager;
    private final AnnotatedType<T> type;

    public PersistenceInjectionTarget(ProcessInjectionTarget<T> event, BeanManager beanManager) {
        this(event.getInjectionTarget(), event.getAnnotatedType(), beanManager);
    }

    public PersistenceInjectionTarget(InjectionTarget<T> delegate, AnnotatedType<T> type, BeanManager beanManager) {
        super(delegate);
        this.type = type;
        this.beanManager = beanManager;
    }

    @Override
    public void inject(T instance, CreationalContext<T> context) {
        super.inject(instance, context);
        type.getFields().forEach(field -> injectField(instance, context, field));
    }

    private void injectField(T instance, CreationalContext<T> context, AnnotatedField<?> field) {
        PersistenceContext persistenceContext = field.getAnnotation(PersistenceContext.class);
        if (persistenceContext != null && EntityManager.class.equals(field.getBaseType())) {
            injectField(instance, context, field, persistenceContext);
        }
        PersistenceUnit persistenceUnit = field.getAnnotation(PersistenceUnit.class);
        if (persistenceUnit != null && EntityManagerFactory.class.equals(field.getBaseType())) {
            injectField(instance, context, field, persistenceUnit);
        }
    }

    private void injectField(T instance, CreationalContext<T> context, AnnotatedField<?> field, Annotation... qualifiers) {
        Set<Bean<?>> beans = beanManager.getBeans(field.getBaseType(), qualifiers);
        Bean<?> bean = beanManager.resolve(beans);
        Object reference = beanManager.getReference(bean, field.getBaseType(), context);
        field.getJavaMember().setAccessible(true);
        try {
            field.getJavaMember().set(instance, reference);
        } catch (IllegalAccessException illegalAccessException) {
            throw new CreationException("Cannot inject into " + field, illegalAccessException);
        }

    }
}
