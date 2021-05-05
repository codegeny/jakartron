package org.codegeny.jakartron.jms;

/*-
 * #%L
 * jakartron-jms
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

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import java.util.concurrent.TimeUnit;

@ExtendWithJakartron
public class JMSTest {

    @Test
    public void test(JMSContext context) {
        Destination in = context.createTemporaryQueue();
        Destination out = context.createTemporaryQueue();
        context.createConsumer(in).setMessageListener(this::onMessage);
        context.createProducer().setJMSReplyTo(out).send(in, "hello");
        String response = context.createConsumer(out).receiveBody(String.class, TimeUnit.SECONDS.toMillis(3));
        Assertions.assertEquals("echo hello", response);
    }

    @Inject
    private ConnectionFactory factory;

    private void onMessage(Message message) {
        try (JMSContext context = factory.createContext()) {
            String body = message.getBody(String.class);
            Destination reply = message.getJMSReplyTo();
            context.createProducer().send(reply, "echo " + body);
        } catch (JMSException exception) {
            throw new JMSRuntimeException(exception.getMessage(), exception.getErrorCode(), exception.getCause());
        }
    }
}
