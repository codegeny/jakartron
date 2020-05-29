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

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.kohsuke.MetaInfServices;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@MetaInfServices(Service.class)
final class TransactionServicesImpl implements TransactionServices {

    private static final Logger LOGGER = Logger.getLogger(TransactionServicesImpl.class.getName());

    private Path temp;

    public TransactionServicesImpl() {
        if (!TransactionManagerServices.isTransactionManagerRunning()) {
            configure(TransactionManagerServices.getConfiguration());
            TransactionManagerServices.getTransactionManager(); // force initialization
        }
    }

    private void configure(Configuration configuration) {
        try {
            temp = Files.createTempDirectory("tx-");
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Cannot create temporary folder for transaction logs", ioException);
        }
        configuration.setDefaultTransactionTimeout(10 * 60);
        configuration.setGracefulShutdownInterval(500);
        configuration.setJournal("null");
        configuration.setServerId(UUID.randomUUID().toString());
        configuration.setLogPart1Filename(temp.resolve("part1.btm").toString());
        configuration.setLogPart2Filename(temp.resolve("part2.btm").toString());
        configuration.setSkipCorruptedLogs(true);
        configuration.setWarnAboutZeroResourceTransaction(false);
    }

    @Override
    public void registerSynchronization(Synchronization synchronizedObserver) {
        TransactionManagerServices.getTransactionSynchronizationRegistry().registerInterposedSynchronization(synchronizedObserver);
    }

    @Override
    public boolean isTransactionActive() {
        return TransactionManagerServices.getTransactionSynchronizationRegistry().getTransactionStatus() == Status.STATUS_ACTIVE;
    }

    @Override
    public UserTransaction getUserTransaction() {
        return TransactionManagerServices.getTransactionManager();
    }

    @Override
    public void cleanup() {
        if (temp != null && TransactionManagerServices.isTransactionManagerRunning()) {
            TransactionManagerServices.getTransactionManager().shutdown();
            try {
                Files.delete(temp);
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, ioException, () -> String.format("Cannot delete temporary folder for transaction logs %s", temp));
            }
        }
    }
}
