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

import org.awaitility.Awaitility;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWithJakartron
public class LockTest {

    @Singleton
    public static class LockedBean {

        @Lock
        public int doSomething(AtomicInteger counter) throws Exception {
            TimeUnit.SECONDS.sleep(2);
            return counter.incrementAndGet();
        }
    }

    @Resource(lookup = "java:comp/concurrent/ThreadPool")
    private ManagedScheduledExecutorService executor;

    @Test
    public void test(LockedBean lockedBean) {
        AtomicInteger counter = new AtomicInteger();
        executor.submit(() -> lockedBean.doSomething(counter));
        executor.submit(() -> lockedBean.doSomething(counter));
        Awaitility.await()
                .atLeast(3, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertEquals(2, counter.get()));

    }
}
