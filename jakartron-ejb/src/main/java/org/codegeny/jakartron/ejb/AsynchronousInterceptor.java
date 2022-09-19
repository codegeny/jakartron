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

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.concurrent.Future;

@Interceptor
@Asynchronous
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class AsynchronousInterceptor {

    @Resource(lookup = "java:comp/concurrent/ThreadPool")
    private ManagedScheduledExecutorService executor;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        if (context.getMethod().getReturnType().equals(void.class)) {
            executor.submit(() -> {
                try {
                    context.proceed();
                } catch (RuntimeException runtimeException) {
                    throw runtimeException;
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
            return null;
        }
        if (Future.class.isAssignableFrom(context.getMethod().getReturnType())) {
            return executor.submit(() -> {
                try {
                    Future<?> future = (Future<?>) context.proceed();
                    return future.get();
                } catch (RuntimeException runtimeException) {
                    throw runtimeException;
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
        throw new UnsupportedOperationException("@Asynchronous methods must return void or Future<>");
    }
}
