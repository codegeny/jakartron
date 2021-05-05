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

import com.arjuna.ats.jta.TransactionManager;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.kohsuke.MetaInfServices;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

@MetaInfServices(Service.class)
final class TransactionServicesImpl implements TransactionServices {

    @Override
    public void registerSynchronization(Synchronization synchronizedObserver) {
        try {
            TransactionManager.transactionManager().getTransaction().registerSynchronization(synchronizedObserver);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean isTransactionActive() {
        try {
            return getUserTransaction().getStatus() == Status.STATUS_ACTIVE;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public UserTransaction getUserTransaction() {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    @Override
    public void cleanup() {

    }
}
