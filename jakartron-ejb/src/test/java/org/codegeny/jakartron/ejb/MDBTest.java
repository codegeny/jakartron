package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.*;

@ExtendWithJakartron
@JMSDestinationDefinition(name = MDBTest.QUEUE_NAME, interfaceName = "javax.jms.Queue")
public class MDBTest {

    public static final String QUEUE_NAME = "myQueue";

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destination", propertyValue = QUEUE_NAME),
            @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
    })
    public static class MyMDB implements MessageListener {

        @Inject
        private JMSContext context;

        @Resource
        private MessageDrivenContext messageDrivenContext;

        @Override
        public void onMessage(Message message) {
            try {
                Assertions.assertEquals("ping", message.getBody(String.class));
                context.createProducer().setJMSCorrelationID(message.getJMSCorrelationID()).send(message.getJMSReplyTo(), "pong");
            } catch (JMSException jmsException) {
                throw new JMSRuntimeException(jmsException.getMessage(), jmsException.getErrorCode(), jmsException.getCause());
            }
        }
    }

    @Resource(lookup = QUEUE_NAME)
    private Queue queue;

    @Test
    public void testPingPong(JMSContext context) throws JMSException {
        TemporaryQueue temporaryQueue = context.createTemporaryQueue();
        try {
            context.createProducer().setJMSReplyTo(temporaryQueue).send(queue, "ping");
            try (JMSConsumer consumer = context.createConsumer(temporaryQueue)) {
                Assertions.assertEquals("pong", consumer.receiveBody(String.class, 5000));
            }
        } finally {
            temporaryQueue.delete();
        }
    }
}
