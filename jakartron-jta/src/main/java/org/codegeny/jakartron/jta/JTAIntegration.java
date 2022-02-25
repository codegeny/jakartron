package org.codegeny.jakartron.jta;

/*-
 * #%L
 * jakartron-jta
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

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import org.codegeny.jakartron.jndi.JNDI;
import org.kohsuke.MetaInfServices;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

@MetaInfServices
public final class JTAIntegration implements Extension {

    public static final String TRANSACTION_MANAGER_JNDI_NAME = "java:/TransactionManager";
    public static final String USER_TRANSACTION_JNDI_NAME = "java:/UserTransaction";
    public static final String TRANSACTION_SYNCHRONIZATION_REGISTRY_JNDI_NAME = "java:/TransactionSynchronizationRegistry";

    public void addTransactionContextAndProducer(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .scope(ApplicationScoped.class)
                .qualifiers(JNDI.Literal.of(TRANSACTION_MANAGER_JNDI_NAME))
                .types(Object.class, TransactionManager.class)
                .createWith(context -> com.arjuna.ats.jta.TransactionManager.transactionManager());
        event.addBean()
                .scope(ApplicationScoped.class)
                .qualifiers(JNDI.Literal.of(USER_TRANSACTION_JNDI_NAME))
                .types(Object.class, UserTransaction.class)
                .createWith(context -> com.arjuna.ats.jta.UserTransaction.userTransaction());
        event.addBean()
                .scope(ApplicationScoped.class)
                .qualifiers(JNDI.Literal.of(TRANSACTION_SYNCHRONIZATION_REGISTRY_JNDI_NAME))
                .types(Object.class, TransactionSynchronizationRegistry.class)
                .createWith(context -> new TransactionSynchronizationRegistryImple());
    }
}
