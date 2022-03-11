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

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ExtendWithJakartron
public class TestEventTest {

    @ApplicationScoped
    public static class Listener {

        private int counter = 0;

        public void beforeAll(@Observes @TestEvent(phase = TestPhase.BEFORE_ALL) Object event) {
            Assertions.assertEquals(0, counter++);
        }

        public void beforeEach(@Observes @TestEvent(phase = TestPhase.BEFORE_EACH) Object event) {
            Assertions.assertEquals(1, counter++);
        }

        public void afterEach(@Observes @TestEvent(phase = TestPhase.AFTER_EACH) Object event) {
            Assertions.assertEquals(2, counter++);
        }

        public void afterAll(@Observes @TestEvent(phase = TestPhase.AFTER_ALL) Object event) {
            Assertions.assertEquals(3, counter++);
        }

        @PreDestroy
        public void destroy() {
            Assertions.assertEquals(4, counter);
        }
    }

    @Test
    public void test() {
    }
}
