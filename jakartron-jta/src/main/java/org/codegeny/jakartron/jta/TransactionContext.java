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

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.transaction.Status;
import javax.transaction.TransactionScoped;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.Arrays;

final class TransactionContext implements Context {

    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    TransactionContext(TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    @Override
    public Class<TransactionScoped> getScope() {
        return TransactionScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        T value = get(contextual);
        if (value == null) {
            transactionSynchronizationRegistry.putResource(contextual, value = contextual.create(creationalContext));
            transactionSynchronizationRegistry.registerInterposedSynchronization(new DestroyingSynchronization<>(value, contextual, creationalContext));
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual) {
        return (T) transactionSynchronizationRegistry.getResource(contextual);
    }

    @Override
    public boolean isActive() {
        return Arrays.asList(Status.STATUS_ACTIVE, Status.STATUS_MARKED_ROLLBACK).contains(transactionSynchronizationRegistry.getTransactionStatus());
    }
}

