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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.security.Principal;

@ExtendWith(LifecycleExtension.class)
@ExtendWithJakartron
public class MocksTest {

    @Inject
    @Mock
    private Principal principal;

    @BeforeEach
    public void configure() {
        Mockito.when(principal.getName()).thenReturn("John");
    }

    @Test
    public void foo() {
        Assertions.assertEquals("John", principal.getName());
    }

    @Test
    public void bar() {
        Assertions.assertEquals("John", principal.getName());
    }
}
