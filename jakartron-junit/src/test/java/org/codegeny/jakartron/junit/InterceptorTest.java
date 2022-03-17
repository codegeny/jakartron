package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWithJakartron
public class InterceptorTest {

    private static final AtomicBoolean FLAG = new AtomicBoolean();

    @Retention(RetentionPolicy.RUNTIME)
    @InterceptorBinding
    public @interface Intercepted {
    }

    @Interceptor
    @Intercepted
    public static class MyInterceptor {

        @AroundInvoke
        public Object intercept(InvocationContext invocationContext) throws Exception {
            FLAG.set(true);
            return invocationContext.proceed();
        }
    }

    @Test
    @Intercepted
    public void test() {
        Assertions.assertTrue(FLAG.get());
    }
}
