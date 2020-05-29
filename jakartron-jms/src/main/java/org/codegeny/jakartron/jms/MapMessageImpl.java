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

import javax.jms.MapMessage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

final class MapMessageImpl extends AbstractMessage<Map<String, Object>> implements MapMessage {

    MapMessageImpl(Map<String, Object> map) {
        super(map);
    }

    MapMessageImpl() {
        super(new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String name) {
        return (T) getBody().get(name);
    }

    @Override
    public boolean getBoolean(String name) {
        return get(name);
    }

    @Override
    public byte getByte(String name) {
        return get(name);
    }

    @Override
    public short getShort(String name) {
        return get(name);
    }

    @Override
    public char getChar(String name) {
        return get(name);
    }

    @Override
    public int getInt(String name) {
        return get(name);
    }

    @Override
    public long getLong(String name) {
        return get(name);
    }

    @Override
    public float getFloat(String name) {
        return get(name);
    }

    @Override
    public double getDouble(String name) {
        return get(name);
    }

    @Override
    public String getString(String name) {
        return get(name);
    }

    @Override
    public byte[] getBytes(String name) {
        return get(name);
    }

    @Override
    public Object getObject(String name) {
        return get(name);
    }

    @Override
    public Enumeration<String> getMapNames() {
        return Collections.enumeration(getBody().keySet());
    }

    @Override
    public void setBoolean(String name, boolean value) {
        getBody().put(name, value);
    }

    @Override
    public void setByte(String name, byte value) {
        getBody().put(name, value);
    }

    @Override
    public void setShort(String name, short value) {
        getBody().put(name, value);
    }

    @Override
    public void setChar(String name, char value) {
        getBody().put(name, value);
    }

    @Override
    public void setInt(String name, int value) {
        getBody().put(name, value);
    }

    @Override
    public void setLong(String name, long value) {
        getBody().put(name, value);
    }

    @Override
    public void setFloat(String name, float value) {
        getBody().put(name, value);
    }

    @Override
    public void setDouble(String name, double value) {
        getBody().put(name, value);
    }

    @Override
    public void setString(String name, String value) {
        getBody().put(name, value);
    }

    @Override
    public void setBytes(String name, byte[] value) {
        getBody().put(name, value);
    }

    @Override
    public void setBytes(String name, byte[] value, int offset, int length) {
        getBody().put(name, value);
    }

    @Override
    public void setObject(String name, Object value) {
        getBody().put(name, value);
    }

    @Override
    public boolean itemExists(String name) {
        return getBody().containsKey(name);
    }
}
