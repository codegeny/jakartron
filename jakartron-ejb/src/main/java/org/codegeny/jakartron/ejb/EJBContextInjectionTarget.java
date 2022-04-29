package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.*;
import java.util.Set;

final class EJBContextInjectionTarget<T> extends InjectionTargetWrapper<T> {

    private final BeanManager beanManager;
    private final AnnotatedType<T> type;

    public EJBContextInjectionTarget(ProcessInjectionTarget<T> event, BeanManager beanManager) {
        this(event.getInjectionTarget(), event.getAnnotatedType(), beanManager);
    }

    public EJBContextInjectionTarget(InjectionTarget<T> delegate, AnnotatedType<T> type, BeanManager beanManager) {
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
        if (field.isAnnotationPresent(Resource.class) && EJBContext.class.isAssignableFrom(field.getJavaMember().getType())) {
            Set<Bean<?>> beans = beanManager.getBeans(field.getBaseType());
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
}
