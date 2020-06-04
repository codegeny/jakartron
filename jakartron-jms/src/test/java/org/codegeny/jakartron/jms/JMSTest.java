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

import org.codegeny.jakartron.AdditionalClasses;
import org.codegeny.jakartron.junit.EnableCDI;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;

@EnableCDI
@AdditionalClasses({JMSProducer.class, JMSIntegration.class})
public class JMSTest {

    @MessageDriven(activationConfig = @ActivationConfigProperty(propertyName = "destination", propertyValue = "testQueue"))
    public static class MyMessageListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                System.out.println("received: " + message.getBody(String.class));
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Resource(name = "testQueue")
    private Queue queue;

    @Test
    public void test(JMSContext context) throws Exception {
        context.createProducer().send(queue, "hello world!");
        Thread.sleep(1000);
    }
}
