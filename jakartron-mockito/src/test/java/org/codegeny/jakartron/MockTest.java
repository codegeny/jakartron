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
import org.codegeny.jakartron.mockito.EnableMockito;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.inject.Inject;

@DisableDiscovery
@ExtendWithJakartron
@EnableMockito
public class MockTest {

    public interface MyService {

        void echo();
    }

    public static class MyServiceImpl implements MyService {

        @Override
        public void echo() {
        }
    }

    public static class Root {

        @Inject
        private MyService service;

        public void run() {
            service.echo();
        }
    }

    @Inject
    @Spy
    MyService service;

    @Inject
    Root root;

    @Test
    public void test() {
        root.run();
        Mockito.verify(service, Mockito.atMostOnce()).echo();
    }

    @Test
    public void test2() {
        root.run();
        Mockito.verify(service, Mockito.atMostOnce()).echo();
    }
}
