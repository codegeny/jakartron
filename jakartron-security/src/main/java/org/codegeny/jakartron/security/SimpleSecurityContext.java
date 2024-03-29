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
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class SimpleSecurityContext implements SecurityContext, SecurityContextController {

    private Set<? extends Principal> principals = new HashSet<>();
    private Set<String> roles = new HashSet<>();

    @Override
    @Produces
    @RequestScoped
    public Principal getCallerPrincipal() {
        return principals.stream().findFirst().orElse(null);
    }

    @Override
    public <T extends Principal> Set<T> getPrincipalsByType(Class<T> pType) {
        return principals.stream().filter(pType::isInstance).map(pType::cast).collect(Collectors.toSet());
    }

    @Override
    public boolean isCallerInRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean hasAccessToWebResource(String resource, String... methods) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthenticationStatus authenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationParameters parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrincipals(Set<? extends Principal> principals) {
        this.principals = principals;
    }

    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public Set<? extends Principal> getPrincipals() {
        return principals;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }
}
