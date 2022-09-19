package org.codegeny.jakartron.dbunit;

/*-
 * #%L
 * jakartron-dbunit
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

import org.codegeny.jakartron.junit.TestEvent;
import org.codegeny.jakartron.junit.TestPhase;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class DBUnitObserver {

    @Inject
    private DBUnitProcessor processor;

    public void before(@Observes @TestEvent(phase = TestPhase.BEFORE_EACH) ExtensionContext context) {
        processor.before(context.getRequiredTestMethod());
    }

    public void after(@Observes @TestEvent(phase = TestPhase.AFTER_EACH) ExtensionContext context) {
        if (!context.getExecutionException().isPresent()) {
            processor.after(context.getRequiredTestMethod());
        }
    }
}
