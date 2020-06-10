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


import com.arjuna.ats.jta.logging.jtaLogger;
import org.jboss.narayana.jta.jms.TransactionHelper;

import javax.jms.BytesMessage;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;
import javax.transaction.Synchronization;
import java.io.Serializable;

public class JMSContextProxy implements JMSContext {

    private final XAJMSContext xaJMSContext;
    private final XAConnectionFactory xaConnectionFactory;
    private final TransactionHelper transactionHelper;

    public JMSContextProxy(XAJMSContext xaJMSContext, XAConnectionFactory xaConnectionFactory, TransactionHelper transactionHelper) {
        this.xaJMSContext = xaJMSContext;
        this.xaConnectionFactory = xaConnectionFactory;
        this.transactionHelper = transactionHelper;
    }

    @Override
    public boolean getTransacted() {
        return xaJMSContext.getTransacted();
    }

    @Override
    public void commit() {
        xaJMSContext.commit();
    }

    @Override
    public void rollback() {
        xaJMSContext.rollback();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        try {
            if (transactionHelper.isTransactionAvailable()) {
                return createAndRegisterContext();
            }
            return xaJMSContext.createContext(sessionMode);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    @Override
    public JMSProducer createProducer() {
        return xaJMSContext.createProducer();
    }

    @Override
    public String getClientID() {
        return xaJMSContext.getClientID();
    }

    @Override
    public void setClientID(String clientID) {
        xaJMSContext.setClientID(clientID);
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return xaJMSContext.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return xaJMSContext.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        xaJMSContext.setExceptionListener(listener);
    }

    @Override
    public void start() {
        xaJMSContext.start();
    }

    @Override
    public void stop() {
        xaJMSContext.stop();
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        xaJMSContext.setAutoStart(autoStart);
    }

    @Override
    public boolean getAutoStart() {
        return xaJMSContext.getAutoStart();
    }

    @Override
    public void close() {
        try {
            if (transactionHelper.isTransactionAvailable()) {
                Synchronization synchronization = new JMSContextClosingSynchronization(xaJMSContext);
                transactionHelper.registerSynchronization(synchronization);

                if (jtaLogger.logger.isTraceEnabled()) {
                    jtaLogger.logger.trace("Registered synchronization to close the connection: " + synchronization);
                }
            } else {
                xaJMSContext.close();
            }
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e.getCause());
        }
    }

    @Override
    public BytesMessage createBytesMessage() {
        return xaJMSContext.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        return xaJMSContext.createMapMessage();
    }

    @Override
    public Message createMessage() {
        return xaJMSContext.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return xaJMSContext.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return xaJMSContext.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return xaJMSContext.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        return xaJMSContext.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return xaJMSContext.createTextMessage(text);
    }

    @Override
    public int getSessionMode() {
        return xaJMSContext.getSessionMode();
    }

    @Override
    public void recover() {
        xaJMSContext.recover();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return xaJMSContext.createConsumer(destination);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return xaJMSContext.createConsumer(destination, messageSelector);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return xaJMSContext.createConsumer(destination, messageSelector, noLocal);
    }

    @Override
    public Queue createQueue(String queueName) {
        return xaJMSContext.createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) {
        return xaJMSContext.createTopic(topicName);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return xaJMSContext.createDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return xaJMSContext.createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return xaJMSContext.createSharedDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return xaJMSContext.createSharedDurableConsumer(topic, name, messageSelector);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return xaJMSContext.createSharedConsumer(topic, sharedSubscriptionName);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return xaJMSContext.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return xaJMSContext.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return xaJMSContext.createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return xaJMSContext.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return xaJMSContext.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) {
        xaJMSContext.unsubscribe(name);
    }

    @Override
    public void acknowledge() {
        xaJMSContext.acknowledge();
    }

    private JMSContext createAndRegisterContext() throws JMSException {
        XAJMSContext xaJMSContext = xaConnectionFactory.createXAContext();
        JMSContext context = new JMSContextProxy(xaJMSContext, xaConnectionFactory, transactionHelper);

        try {
            transactionHelper.registerXAResource(xaJMSContext.getXAResource());
        } catch (RuntimeException e) {
            xaJMSContext.close();
            throw e;
        }

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied context: " + xaJMSContext);
        }

        return context;
    }
}
