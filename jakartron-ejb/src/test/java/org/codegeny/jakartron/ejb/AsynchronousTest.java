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

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@ExtendWithJakartron
public class AsynchronousTest {

    @ApplicationScoped
    public static class AsynchronousBean {

        @Asynchronous
        public Future<Integer> doSomething() throws Exception {
            TimeUnit.SECONDS.sleep(5);
            return new AsyncResult<>(42);
        }
    }

    @Test
    public void test(AsynchronousBean asynchronousBean) throws Exception {
        Future<Integer> future = asynchronousBean.doSomething();
        Awaitility.await()
                .atLeast(4, TimeUnit.SECONDS)
                .atMost(6, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertTrue(future.isDone()));
        Assertions.assertEquals(42, future.get());
    }
}
