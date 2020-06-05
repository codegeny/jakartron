package org.codegeny.jakartron.security;

/*-
 * #%L
 * jakartron-security
 * %%
 * Copyright (C) 2018 - 2020 Codegeny
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

@Interceptor
@RunAsUser(name = "dummy")
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
public class RunAsInterceptor {

    @Inject
    private PrincipalHolder principalHolder;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        WeldInvocationContext weldInvocationContext = (WeldInvocationContext) context;
        RunAsUser runAs = weldInvocationContext.getInterceptorBindingsByType(RunAsUser.class).stream().findFirst().orElseThrow(InternalError::new);
        Principal old = principalHolder.getPrincipal();
        principalHolder.setPrincipal(runAs::name);
        try {
            return context.proceed();
        } finally {
            principalHolder.setPrincipal(old);
        }
    }
}
