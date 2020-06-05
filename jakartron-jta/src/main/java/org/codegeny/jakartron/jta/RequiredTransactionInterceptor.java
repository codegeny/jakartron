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

import org.jboss.weld.interceptor.WeldInvocationContext;

import javax.annotation.Priority;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.*;
import java.util.stream.Stream;

@Interceptor
@Transactional
@Priority(Interceptor.Priority.PLATFORM_AFTER + 100)
final class RequiredTransactionInterceptor {

    @Inject
    private TransactionManager transactionManager;

    @Inject
    @Initialized(TransactionScoped.class)
    private Event<Transaction> initialized;

    @Inject
    @Destroyed(TransactionScoped.class)
    private Event<Transaction> destroyed;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        // Non-portable until https://issues.jboss.org/browse/CDI-468 is resolved
        WeldInvocationContext weldContext = (WeldInvocationContext) context;
        Transactional transactional = weldContext.getInterceptorBindingsByType(Transactional.class)
                .stream()
                .findAny()
                .orElseThrow(InternalError::new);

        if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
            return context.proceed();
        }

        transactionManager.begin();
        Transaction transaction = transactionManager.getTransaction();
        initialized.fire(transaction);

        try {
            Object result = context.proceed();
            transactionManager.commit();
            return result;
        } catch (Throwable exception) {
            if (rollback(exception, transactional)) {
                transactionManager.rollback();
            } else {
                transactionManager.commit();
            }
            throw exception;
        } finally {
            destroyed.fire(transaction);
        }
    }

    private boolean rollback(Throwable exception, Transactional transactional) {
        if (Stream.of(transactional.rollbackOn()).anyMatch(c -> c.isInstance(exception))) {
            return true;
        } else if (Stream.of(transactional.dontRollbackOn()).anyMatch(c -> c.isInstance(exception))) {
            return false;
        } else {
            return exception instanceof RuntimeException || exception instanceof Error;
        }
        // TODO add @ApplicationException handling but this comes from EJB, not JTA.
    }
}
