package org.codegeny.jakartron.mailra;

/*-
 * #%L
 * jakartron-mailra
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

import org.codegeny.jakartron.jca.ConfigureResourceAdapter;
import org.wildfly.mail.ra.MailActivationSpec;
import org.wildfly.mail.ra.MailListener;
import org.wildfly.mail.ra.MailResourceAdapter;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;

@Dependent
public class MailRAProducer {

    public void registerAdapter(@Observes ConfigureResourceAdapter event) {
        event.setResourceAdapter(MailListener.class, new MailResourceAdapter(), MailActivationSpec.class);
    }
}

