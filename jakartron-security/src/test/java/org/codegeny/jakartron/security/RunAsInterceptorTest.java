package org.codegeny.jakartron.security;

/*-
 * #%L
 * jakartron-security
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
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.IllegalProductException;
import javax.security.enterprise.SecurityContext;
import java.security.Principal;

@ExtendWithJakartron
public class RunAsInterceptorTest {

    @Test
    @RunAsUser(name = "foobar")
    public void testPresent(Principal principal) {
        Assertions.assertEquals("foobar", principal.getName());
    }

    @Test
    @RunAsUser(name = "foobar", roles = {"foo", "bar"})
    public void testPresent(SecurityContext securityContext) {
        Assertions.assertEquals("foobar", securityContext.getCallerPrincipal().getName());
        Assertions.assertTrue(securityContext.isCallerInRole("foo"));
        Assertions.assertTrue(securityContext.isCallerInRole("bar"));
        Assertions.assertFalse(securityContext.isCallerInRole("baz"));
    }

    @Test
    public void testAbsent(Principal principal) {
        Assertions.assertThrows(IllegalProductException.class, () -> Assertions.assertNotEquals("foobar", principal.getName()));
    }

    @Test
    public void testAbsent(SecurityContext securityContext) {
        Assertions.assertNull(securityContext.getCallerPrincipal());
        Assertions.assertFalse(securityContext.isCallerInRole("foo"));
        Assertions.assertFalse(securityContext.isCallerInRole("bar"));
        Assertions.assertFalse(securityContext.isCallerInRole("baz"));
    }
}
