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

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import org.codegeny.jakartron.jndi.JNDI;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

@MetaInfServices
public final class JTAIntegration implements Extension {

    public void processResources(@Observes @WithAnnotations(Resource.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && !f.getAnnotation(Resource.class).lookup().isEmpty())
                .forEach(f -> f.add(InjectLiteral.INSTANCE).add(JNDI.Literal.of(f.getAnnotated().getAnnotation(Resource.class).lookup())));
    }

    public void addTransactionContextAndProducer(@Observes AfterBeanDiscovery event) {
//        event.addObserverMethod().observedType(Object.class).qualifiers(Initialized.Literal.of(TransactionScoped.class)).notifyWith(context -> System.out.println("<TX>"));
//        event.addObserverMethod().observedType(Object.class).qualifiers(Destroyed.Literal.of(TransactionScoped.class)).notifyWith(context -> System.out.println("</TX>"));

        event.addBean()
                .scope(ApplicationScoped.class)
                .qualifiers(JNDI.Literal.of("java:/TransactionManager"))
                .types(Object.class, TransactionManager.class)
                .createWith(context -> com.arjuna.ats.jta.TransactionManager.transactionManager());
        event.addBean()
                .scope(ApplicationScoped.class)
                .qualifiers(JNDI.Literal.of("java:/UserTransaction"))
                .types(Object.class, UserTransaction.class)
                .createWith(context -> com.arjuna.ats.jta.UserTransaction.userTransaction());
        event.addBean()
                .scope(ApplicationScoped.class)
                .qualifiers(JNDI.Literal.of("java:/TransactionSynchronizationRegistry"))
                .types(Object.class, TransactionSynchronizationRegistry.class)
                .createWith(context -> new TransactionSynchronizationRegistryImple());
    }
}
