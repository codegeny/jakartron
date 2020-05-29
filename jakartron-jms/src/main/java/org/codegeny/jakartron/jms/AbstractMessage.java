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

import javax.jms.Destination;
import javax.jms.Message;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractMessage<B> implements Message {

    private static final String DELIVERY_MODE = "$JMS_DELIVERY_MODE$";
    private static final String MESSAGE_ID = "$JMS_MESSAGE_ID$";
    private static final String TIMESTAMP = "$JMS_TIMESTAMP$";
    private static final String CORRELATION_ID = "$JMS_CORRELATION_ID$";
    private static final String REPLY_TO = "$JMS_REPLY_TO$";
    private static final String REDELIVERED = "$JMS_REDELIVERED$";
    private static final String TYPE = "$JMS_TYPE$";
    private static final String EXPIRATION = "$JMS_EXPIRATION$";
    private static final String DELIVERY_TIME = "$JMS_DELIVERY_TIME$";
    private static final String PRIORITY = "$JMS_PRIORITY$";
    private static final String DESTINATION = "$JMS_DESTINATION$";

    private final Map<String, Object> properties = new HashMap<>();
    private B body;

    AbstractMessage(B body) {
        this.body = body;
    }

    AbstractMessage() {
    }

    B getBody() {
        return body;
    }

    void setBody(B body) {
        this.body = body;
    }

    @Override
    public String getJMSMessageID() {
        return getStringProperty(MESSAGE_ID);
    }

    @Override
    public void setJMSMessageID(String id) {
        setStringProperty(MESSAGE_ID, id);
    }

    @Override
    public long getJMSTimestamp() {
        return getLongProperty(TIMESTAMP);
    }

    @Override
    public void setJMSTimestamp(long timestamp) {
        setLongProperty(TIMESTAMP, timestamp);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return getJMSCorrelationID().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) {
        setJMSCorrelationID(new String(correlationID, StandardCharsets.UTF_8));
    }

    @Override
    public String getJMSCorrelationID() {
        return getStringProperty(CORRELATION_ID);
    }

    @Override
    public void setJMSCorrelationID(String correlationID) {
        setStringProperty(CORRELATION_ID, correlationID);
    }

    @Override
    public Destination getJMSReplyTo() {
        return (Destination) getObjectProperty(REPLY_TO);
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) {
        setObjectProperty(REPLY_TO, replyTo);
    }

    @Override
    public Destination getJMSDestination() {
        return (Destination) getObjectProperty(DESTINATION);
    }

    @Override
    public void setJMSDestination(Destination destination) {
        setObjectProperty(DESTINATION, destination);
    }

    @Override
    public int getJMSDeliveryMode() {
        return getIntProperty(DELIVERY_MODE);
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) {
        setIntProperty(DELIVERY_MODE, deliveryMode);
    }

    @Override
    public boolean getJMSRedelivered() {
        return getBooleanProperty(REDELIVERED);
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) {
        setBooleanProperty(REDELIVERED, redelivered);
    }

    @Override
    public String getJMSType() {
        return getStringProperty(TYPE);
    }

    @Override
    public void setJMSType(String type) {
        setStringProperty(TYPE, type);
    }

    @Override
    public long getJMSExpiration() {
        return getLongProperty(EXPIRATION);
    }

    @Override
    public void setJMSExpiration(long expiration) {
        setLongProperty(EXPIRATION, expiration);
    }

    @Override
    public long getJMSDeliveryTime() {
        return getLongProperty(DELIVERY_TIME);
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) {
        setLongProperty(DELIVERY_TIME, deliveryTime);
    }

    @Override
    public int getJMSPriority() {
        return getIntProperty(PRIORITY);
    }

    @Override
    public void setJMSPriority(int priority) {
        setIntProperty(PRIORITY, priority);
    }

    @Override
    public void clearProperties() {
        properties.clear();
    }

    @Override
    public boolean propertyExists(String name) {
        return properties.containsKey(name);
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return (boolean) getObjectProperty(name);
    }

    @Override
    public byte getByteProperty(String name) {
        return (byte) getObjectProperty(name);
    }

    @Override
    public short getShortProperty(String name) {
        return (short) getObjectProperty(name);
    }

    @Override
    public int getIntProperty(String name) {
        return (int) getObjectProperty(name);
    }

    @Override
    public long getLongProperty(String name) {
        return (long) getObjectProperty(name);
    }

    @Override
    public float getFloatProperty(String name) {
        return (float) getObjectProperty(name);
    }

    @Override
    public double getDoubleProperty(String name) {
        return (double) getObjectProperty(name);
    }

    @Override
    public String getStringProperty(String name) {
        return (String) getObjectProperty(name);
    }

    @Override
    public Object getObjectProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Enumeration<String> getPropertyNames() {
        return Collections.enumeration(properties.keySet().stream().filter(s -> !s.startsWith("$JMS_") && !s.endsWith("$")).collect(Collectors.toList()));
    }

    @Override
    public void setBooleanProperty(String name, boolean value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setByteProperty(String name, byte value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setShortProperty(String name, short value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setIntProperty(String name, int value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setLongProperty(String name, long value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setFloatProperty(String name, float value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setDoubleProperty(String name, double value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setStringProperty(String name, String value) {
        setObjectProperty(name, value);
    }

    @Override
    public void setObjectProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public void acknowledge() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearBody() {
        this.body = null;
    }

    @Override
    public <T> T getBody(Class<T> c) {
        return c.cast(body);
    }

    @Override
    public boolean isBodyAssignableTo(@SuppressWarnings("rawtypes") Class c) {
        return c.isInstance(body);
    }

    @Override
    public String toString() {
        return String.valueOf(body);
    }
}
