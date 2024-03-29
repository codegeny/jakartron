package org.codegeny.jakartron.jaxws;

/*-
 * #%L
 * jakartron-jaxws
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

import com.sun.xml.ws.api.server.Container;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class ServletContainer extends Container {

    private final Set<?> components;

    ServletContainer(Object... components) {
        this.components = new HashSet<>(Arrays.asList(components));
    }

    @Override
    public <T> T getSPI(Class<T> spiType) {
        return components.stream().filter(spiType::isInstance).map(spiType::cast).findFirst().orElse(null);
    }
}
