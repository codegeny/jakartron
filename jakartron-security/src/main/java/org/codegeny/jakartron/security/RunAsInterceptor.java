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

import org.jboss.weld.interceptor.WeldInvocationContext;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Interceptor
@RunAsUser(name = "dummy")
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
public class RunAsInterceptor {

    @Inject
    private SecurityContextController controller;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        WeldInvocationContext weldInvocationContext = (WeldInvocationContext) context;
        RunAsUser runAs = weldInvocationContext.getInterceptorBindingsByType(RunAsUser.class).stream().findFirst().orElseThrow(InternalError::new);
        Set<? extends Principal> principals = controller.getPrincipals();
        Set<String> roles = controller.getRoles();
        controller.setPrincipal(runAs::name);
        controller.setRoles(new HashSet<>(Arrays.asList(runAs.roles())));
        try {
            return context.proceed();
        } finally {
            controller.setPrincipals(principals);
            controller.setRoles(roles);
        }
    }
}
