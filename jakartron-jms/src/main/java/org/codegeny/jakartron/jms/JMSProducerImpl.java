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
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.*;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
class JMSProducerImpl implements JMSProducer {

    @Inject
    private Event<JMSEvent> event;

    @Override
    public JMSProducer send(Destination destination, Message message) {
        event.fire(new JMSEvent(destination.toString(), message));
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, String body) {
        return send(destination, new TextMessageImpl(body));
    }

    @Override
    public JMSProducer send(Destination destination, Map<String, Object> body) {
        return send(destination, new MapMessageImpl(body));
    }

    @Override
    public JMSProducer send(Destination destination, byte[] body) {
        try {
            return send(destination, new BytesMessageImpl(body));
        } catch (JMSException jmsException) {
            throw new JMSRuntimeException(jmsException.getMessage(), jmsException.getErrorCode(), jmsException.getLinkedException());
        }
    }

    @Override
    public JMSProducer send(Destination destination, Serializable body) {
        return send(destination, new ObjectMessageImpl(body));
    }

    @Override
    public JMSProducer setDisableMessageID(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getDisableMessageID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setDisableMessageTimestamp(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getDisableMessageTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setDeliveryMode(int deliveryMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDeliveryMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setPriority(int priority) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setTimeToLive(long timeToLive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeToLive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setDeliveryDelay(long deliveryDelay) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDeliveryDelay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setAsync(CompletionListener completionListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionListener getAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, short value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setProperty(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer clearProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean propertyExists(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBooleanProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByteProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShortProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLongProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloatProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDoubleProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObjectProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getPropertyNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setJMSCorrelationIDAsBytes(byte[] correlationID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setJMSCorrelationID(String correlationID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJMSCorrelationID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setJMSType(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJMSType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSProducer setJMSReplyTo(Destination replyTo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Destination getJMSReplyTo() {
        throw new UnsupportedOperationException();
    }
}
