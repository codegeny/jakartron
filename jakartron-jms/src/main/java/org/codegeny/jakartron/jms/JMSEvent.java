package org.codegeny.jakartron.jms;

/*-
 * #%L
 * jakartron-jms
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

import javax.enterprise.event.Event;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.jms.Message;

final class JMSEvent {

    private final String destination;
    private final Message message;

    JMSEvent(String destination, Message message) {
        this.destination = destination;
        this.message = message;
    }

    void fire(Event<Message> event) {
        event.select(NamedLiteral.of(destination)).fireAsync(message);
    }
}
