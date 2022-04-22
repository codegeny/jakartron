package org.codegeny.jakartron;

/*-
 * #%L
 * jakartron-mockito
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
import org.codegeny.jakartron.mockito.EnableAutoMocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ExtendWithJakartron
@EnableAutoMocks
public class ScopeTest {

    public interface MyDependency {

        String hello();
    }

    @ApplicationScoped
    public static class MyService {

        @Inject
        private MyDependency dependency;

        public String hello() {
            return dependency.hello() + " world!";
        }
    }

    @Inject
    private MyService service;

    @Inject
    private MyDependency dependency;

    @Test
    public void test() {
        Mockito.when(dependency.hello()).thenReturn("HELLO");
        Assertions.assertEquals("HELLO world!", service.hello());
        Mockito.verify(dependency, Mockito.times(1)).hello();
    }

    @Test
    public void test2() {
        Mockito.when(dependency.hello()).thenReturn("HELLO");
        Assertions.assertEquals("HELLO world!", service.hello());
        Mockito.verify(dependency, Mockito.times(1)).hello();
    }
}
