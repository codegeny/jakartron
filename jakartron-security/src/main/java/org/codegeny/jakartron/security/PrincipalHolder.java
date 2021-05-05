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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import java.security.Principal;

@RequestScoped
public class PrincipalHolder {

    private Principal principal;

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    @Produces
    @RequestScoped
    public Principal getPrincipal() {
        return principal;
    }
}
