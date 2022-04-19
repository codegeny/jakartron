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

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public interface SecurityContextController {

    default void setPrincipal(Principal principal) {
        setPrincipals(Collections.singleton(principal));
    }

    void setPrincipals(Set<? extends Principal> principals);

    void setRoles(Set<String> roles);

    Set<? extends Principal> getPrincipals();

    Set<String> getRoles();
}
