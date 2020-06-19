package org.codegeny.jakartron.ejb;/*-
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

import org.awaitility.Awaitility;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.transaction.Transactional;

@ExtendWithJakartron
public class JMSTest {

    @MessageDriven(activationConfig = @ActivationConfigProperty(propertyName = "destination", propertyValue = "testQueue"))
    public static class MyMessageListener implements MessageListener {

        private static volatile boolean received = false;

        @Override
        public void onMessage(Message message) {
            try {
                received = message.getBody(String.class).equals("hello world!");
            } catch (JMSException jmsException) {
                throw new RuntimeException(jmsException);
            }
        }
    }

    @Transactional
    public static class MyMessageSender {

        @Resource(lookup = "testQueue")
        private Queue queue;

        @Inject
        private JMSContext context;

        public void send(String message) throws Exception {
             context.createProducer().send(queue, "hello world!");
             Thread.sleep(1000);
             System.out.println("end of tx");
        }
    }

    @Test
    public void test(MyMessageSender sender) throws Exception {
        sender.send("hello world!");
        Awaitility.await().until(() -> MyMessageListener.received);
    }
}
