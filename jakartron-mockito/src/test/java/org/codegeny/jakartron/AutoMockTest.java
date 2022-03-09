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
public class AutoMockTest {

    // No implementation
    public interface Service {

        int answer();
    }

    @ApplicationScoped
    public static class Facade {

        @Inject
        private Service service;

        public int answerPlusOne() {
            return service.answer() + 1;
        }
    }

    @Inject
    private Service service; // non resolvable -> mock

    @Inject
    private Facade facade;

    @Test
    public void test() {
        Mockito.when(service.answer()).thenReturn(42);
        Assertions.assertEquals(43, facade.answerPlusOne());
    }
}
