package org.codegeny.jakartron.jms;

/*-
 * #%L
 * jakartron-jms
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

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

public abstract class TransactionalMessageEndpoint implements MessageEndpoint {

    private final TransactionManager transactionManager;
    private final XAResource resource;

    public TransactionalMessageEndpoint(TransactionManager transactionManager, XAResource resource) {
        this.transactionManager = transactionManager;
        this.resource = resource;
    }

    public void beforeDelivery(Method method) throws ResourceException {
        try {
            transactionManager.begin();
            transactionManager.getTransaction().enlistResource(resource);
        } catch (SystemException | RollbackException | NotSupportedException exception) {
            throw new ResourceException(exception);
        }
    }

    public void afterDelivery() throws ResourceException {
        try {
            if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                transactionManager.commit();
            } else {
                transactionManager.rollback();
            }
        } catch (SystemException | RollbackException | HeuristicRollbackException | HeuristicMixedException exception) {
            throw new ResourceException(exception);
        }
    }
}
