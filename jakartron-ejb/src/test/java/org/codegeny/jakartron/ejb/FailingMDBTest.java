package org.codegeny.jakartron.ejb;/*-
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

import org.awaitility.Awaitility;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.util.concurrent.TimeUnit;

@ExtendWithJakartron
public class FailingMDBTest {

    private static volatile int received;
    private static final String QUEUE_NAME = "myQueue";

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destination", propertyValue = QUEUE_NAME),
            @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
    })
    public static class MyMDB implements MessageListener {

        @Override
        public void onMessage(Message message) {
            received++;
            throw new EJBException();
        }
    }

    @Resource(lookup = QUEUE_NAME)
    private Queue queue;

    @Test
    @DisabledIfSystemProperty(named = "user.name", matches = "travis")
    public void testProxy(JMSContext context) {
        context.createProducer().send(queue, "ping");
        Awaitility.await().pollDelay(5, TimeUnit.SECONDS).until(() -> received == 3); // because redeliveryAttempts = 3
    }
}
