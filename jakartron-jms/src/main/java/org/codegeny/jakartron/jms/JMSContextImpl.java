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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import java.io.Serializable;

@ApplicationScoped
class JMSContextImpl implements JMSContext {

    @Inject
    private JMSProducer producer;

    @Override
    public JMSContext createContext(int sessionMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer createProducer() {
        return producer;
    }

    @Override
    public String getClientID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientID(String clientID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionMetaData getMetaData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAutoStart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesMessage createBytesMessage() {
        return new BytesMessageImpl();
    }

    @Override
    public MapMessage createMapMessage() {
        return new MapMessageImpl();
    }

    @Override
    public Message createMessage() {
        return new MessageImpl();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return new ObjectMessageImpl();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return new ObjectMessageImpl(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return new BytesMessageImpl();
    }

    @Override
    public TextMessage createTextMessage() {
        return new TextMessageImpl();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return new TextMessageImpl(text);
    }

    @Override
    public boolean getTransacted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSessionMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recover() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Queue createQueue(String queueName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Topic createTopic(String topicName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribe(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acknowledge() {
        throw new UnsupportedOperationException();
    }
}

