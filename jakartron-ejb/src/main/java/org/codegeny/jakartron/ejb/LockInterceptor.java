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
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.LockType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Interceptor
@javax.ejb.Lock
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class LockInterceptor {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        Optional<Lock> optionalLock = getLock(invocationContext);
        if (!optionalLock.isPresent()) {
            return invocationContext.proceed();
        }
        Lock lock = optionalLock.get();
        Optional<AccessTimeout> optionalAccessTimeout = getAnnotation(invocationContext, AccessTimeout.class);
        if (!optionalAccessTimeout.isPresent()) {
            lock.lock();
        } else {
            AccessTimeout accessTimeout = optionalAccessTimeout.get();
            if (!lock.tryLock(accessTimeout.value(), accessTimeout.unit())) {
                throw new ConcurrentAccessTimeoutException();
            }
        }
        try {
            return invocationContext.proceed();
        } finally {
            lock.unlock();
        }
    }

    private <A extends Annotation> Optional<A> getAnnotation(InvocationContext invocationContext, Class<A> annotationType) {
        A annotation = invocationContext.getMethod().getAnnotation(annotationType);
        return annotation != null
                ? Optional.of(annotation)
                : Optional.ofNullable(invocationContext.getMethod().getDeclaringClass().getAnnotation(annotationType));
    }

    private Optional<Lock> getLock(InvocationContext invocationContext) {
        return getAnnotation(invocationContext, javax.ejb.Lock.class).map(javax.ejb.Lock::value)
                .map(lockType -> lockType == LockType.WRITE ? readWriteLock.writeLock() : readWriteLock.readLock());
    }
}
