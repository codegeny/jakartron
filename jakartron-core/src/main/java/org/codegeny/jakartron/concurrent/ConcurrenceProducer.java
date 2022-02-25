package org.codegeny.jakartron.concurrent;

/*-
 * #%L
 * jakartron-core
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

import org.codegeny.jakartron.Internal;
import org.codegeny.jakartron.jndi.JNDI;

import javax.enterprise.concurrent.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Dependent
public class ConcurrenceProducer {

    public static final String MANAGED_EXECUTOR_SERVICE_JNDI_NAME = "java:comp/concurrent/ThreadPool";

    @Inject
    @Internal
    private Logger logger;

    @Produces
    @ApplicationScoped
    public ManagedThreadFactory createManagedThreadFactory() {
        return ManageableThreadImpl::new;
    }

    @Produces
    @JNDI(MANAGED_EXECUTOR_SERVICE_JNDI_NAME)
    @ApplicationScoped
    public ManagedScheduledExecutorService createManagedScheduledExecutorService(ManagedThreadFactory managedThreadFactory) {
        return new ManagedScheduledExecutorServiceImpl(managedThreadFactory);
    }

    public void destroyManagedExecutorService(@Disposes @JNDI(MANAGED_EXECUTOR_SERVICE_JNDI_NAME) ManagedExecutorService managedExecutorService) throws InterruptedException {
        managedExecutorService.shutdown();
        if (!managedExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.warning("Could not shut down ManagedExecutorService properly");
        }
    }

    private static final class ManagedScheduledExecutorServiceImpl extends ScheduledThreadPoolExecutor implements ManagedScheduledExecutorService {

        ManagedScheduledExecutorServiceImpl(ThreadFactory threadFactory) {
            super(5, threadFactory);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, Trigger trigger) {
            return schedule(() -> {
                command.run();
                return null;
            }, trigger);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, Trigger trigger) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ManageableThreadImpl extends Thread implements ManageableThread {

        ManageableThreadImpl(Runnable target) {
            super(target);
        }

        @Override
        public boolean isShutdown() {
            return false;
        }
    }
}
