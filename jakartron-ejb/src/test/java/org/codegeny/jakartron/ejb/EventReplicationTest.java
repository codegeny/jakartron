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
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.EventMetadata;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.jms.*;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

@ExtendWithJakartron
public class EventReplicationTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface Clustered {
    }

    private static final String TOPIC_NAME = "eventTopic";

    public static class EventMessage implements Serializable {

        private final Serializable event;
        private final Annotation[] qualifiers;

        public EventMessage(Serializable event, Annotation... qualifiers) {
            this.event = event;
            this.qualifiers = qualifiers;
        }

        public Serializable getEvent() {
            return event;
        }

        public Annotation[] getQualifiers() {
            return qualifiers;
        }
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destination", propertyValue = TOPIC_NAME),
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    })
    public static class EventReplicator implements MessageListener {

        @Inject
        private Event<Object> event;

        @Override
        public void onMessage(Message message) {
            try {
                EventMessage eventMessage = message.getBody(EventMessage.class);
                event.select(eventMessage.getQualifiers()).fire(eventMessage.getEvent());
            } catch (JMSException jmsException) {
                throw new EJBException(jmsException);
            }
        }
    }

    @ApplicationScoped
    public static class EventListener {

        @Inject
        private JMSContext jmsContext;

        @Resource(lookup = TOPIC_NAME)
        private Topic topic;

        public void observer(@Observes @Clustered Serializable event, EventMetadata metadata) {
            jmsContext.createProducer().send(topic, new EventMessage(event, metadata.getQualifiers().stream().filter(q -> !(q instanceof Clustered)).toArray(Annotation[]::new)));
        }
    }

    private final AtomicInteger counter = new AtomicInteger();

    @Test
    public void test(@Clustered Event<Integer> event) {
        event.fire(42);
        // wait for the event to be received twice: one normally, one through the MDB
        await().untilAtomic(counter, is(2));
    }

    public void observes(@Observes Integer event) {
        counter.incrementAndGet();
    }
}
