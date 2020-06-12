package org.codegeny.jakartron;

/*-
 * #%L
 * jakartron-core
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

import org.kohsuke.MetaInfServices;

import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import java.util.ArrayDeque;
import java.util.Deque;

@MetaInfServices
public class CoreExtension implements Extension {

    public interface Hook {

        void run() throws Exception;
    }

    private final Deque<Hook> shutdownHooks = new ArrayDeque<>();

    public void addShutdownHook(Hook hook) {
        shutdownHooks.push(hook);
    }

    public void addObserver(@Observes AfterBeanDiscovery event) {
        event.addObserverMethod()
                .observedType(Object.class)
                .qualifiers(Destroyed.Literal.APPLICATION)
                .notifyWith(context -> onShutdown());
    }

    private void onShutdown() {
        while (!shutdownHooks.isEmpty()) {
            try {
                shutdownHooks.pop().run();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
