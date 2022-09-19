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

import org.kohsuke.MetaInfServices;

import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

@MetaInfServices
public final class EJBExtension implements Extension {

    private final Collection<Class<? extends Annotation>> ejbAnnotations = Arrays.asList(Stateless.class, Singleton.class, Stateful.class, MessageDriven.class);

    public <T> void inject(@Observes ProcessInjectionTarget<T> event, BeanManager beanManager) {
        if (event.getAnnotatedType().getAnnotations().stream().map(Annotation::annotationType).anyMatch(ejbAnnotations::contains)) {
            event.setInjectionTarget(new EJBContextInjectionTarget<>(event, beanManager));
        }
    }

    public void scope(@Observes @WithAnnotations({Stateless.class, Singleton.class}) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType().add(ApplicationScoped.Literal.INSTANCE);
    }
}
