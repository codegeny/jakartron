package org.codegeny.jakartron;

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

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import javax.transaction.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ExtendWithJakartron
public class JTARollbackOnlyTest {

    @Retention(RetentionPolicy.RUNTIME)
    @InterceptorBinding
    public @interface RollbackOnly {
    }

    @Interceptor
    @RollbackOnly
    public static class RollbackOnlyInterceptor {

        @Inject
        private TransactionManager transactionManager;

        @AroundInvoke
        public Object rollback(InvocationContext invocationContext) throws Exception {
            try {
                return invocationContext.proceed();
            } finally {
                if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                    transactionManager.setRollbackOnly();
                }
            }
        }
    }

    @Test
    @Transactional
    @RollbackOnly
    public void test(TransactionSynchronizationRegistry registry) {
        registry.registerInterposedSynchronization(new Synchronization() {

            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                Assertions.assertEquals(Status.STATUS_ROLLEDBACK, status);
            }
        });
    }
}
