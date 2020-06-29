package org.codegeny.jakartron.ejb;
/*-
 * #%L
 * jakartron-ejb
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

import org.codegeny.jakartron.DisableDiscovery;
import org.codegeny.jakartron.jmsra.EnableJMSRA;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.util.concurrent.TimeUnit;

@ExtendWithJakartron
@EnableEJB
@EnableJMSRA
@DisableDiscovery
public class ModularTest {

    private static final String QUEUE_NAME = "myQueue";

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destination", propertyValue = QUEUE_NAME),
            @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
    })
    public static class MyMDB implements MessageListener {

        @Inject
        @JMSConnectionFactory("java:/JmsXA")
        private JMSContext context;

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
    public void test(JMSContext context) {
        Destination temporaryQueue = context.createTemporaryQueue();
        context.createProducer().setJMSReplyTo(temporaryQueue).send(queue, "ping");
        Assertions.assertEquals("pong", context.createConsumer(temporaryQueue).receiveBody(String.class, TimeUnit.SECONDS.toMillis(5)));
    }
}
