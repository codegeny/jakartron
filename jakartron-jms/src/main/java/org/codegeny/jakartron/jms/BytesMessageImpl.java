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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.StreamMessage;
import java.io.*;

final class BytesMessageImpl extends AbstractMessage<byte[]> implements BytesMessage, StreamMessage {

    private ObjectInputStream input;
    private ByteArrayOutputStream bytes;
    private DataOutputStream output;

    BytesMessageImpl(byte[] body) throws JMSException {
        super(body);
        this.input = get(() -> new ObjectInputStream(new ByteArrayInputStream(body)));
    }

    BytesMessageImpl() {
        clearBody();
    }

    private <T> T get(IOSupplier<T> supplier) throws JMSException {
        try {
            return supplier.get();
        } catch (IOException ioException) {
            throw new JMSException(ioException.getMessage());
        }
    }

    private void run(IORunnable runnable) throws JMSException {
        try {
            runnable.run();
        } catch (IOException ioException) {
            throw new JMSException(ioException.getMessage());
        }
    }

    @Override
    public void clearBody() {
        this.output = new DataOutputStream(this.bytes = new ByteArrayOutputStream());
    }

    @Override
    public void reset() throws JMSException {
        run(output::close);
        setBody(bytes.toByteArray());
        input = get(() -> new ObjectInputStream(new ByteArrayInputStream(getBody())));
    }

    @Override
    public long getBodyLength() {
        return getBody().length;
    }

    @Override
    public boolean readBoolean() throws JMSException {
        return get(input::readBoolean);
    }

    @Override
    public byte readByte() throws JMSException {
        return get(input::readByte);
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        return get(input::readUnsignedByte);
    }

    @Override
    public short readShort() throws JMSException {
        return get(input::readShort);
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        return get(input::readUnsignedShort);
    }

    @Override
    public char readChar() throws JMSException {
        return get(input::readChar);
    }

    @Override
    public int readInt() throws JMSException {
        return get(input::readInt);
    }

    @Override
    public long readLong() throws JMSException {
        return get(input::readLong);
    }

    @Override
    public float readFloat() throws JMSException {
        return get(input::readFloat);
    }

    @Override
    public double readDouble() throws JMSException {
        return get(input::readDouble);
    }

    @Override
    public String readUTF() throws JMSException {
        return get(input::readUTF);
    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        return get(() -> input.read(value));
    }

    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        return get(() -> input.read(value, 0, length));
    }

    @Override
    public String readString() throws JMSException {
        return get(input::readUTF);
    }

    @Override
    public Object readObject() throws JMSException {
        try {
            return input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            throw new JMSException(exception.getMessage());
        }
    }

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        run(() -> output.writeBoolean(value));
    }

    @Override
    public void writeByte(byte value) throws JMSException {
        run(() -> output.writeByte(value));
    }

    @Override
    public void writeShort(short value) throws JMSException {
        run(() -> output.writeShort(value));
    }

    @Override
    public void writeChar(char value) throws JMSException {
        run(() -> output.writeChar(value));
    }

    @Override
    public void writeInt(int value) throws JMSException {
        run(() -> output.writeInt(value));
    }

    @Override
    public void writeLong(long value) throws JMSException {
        run(() -> output.writeLong(value));
    }

    @Override
    public void writeFloat(float value) throws JMSException {
        run(() -> output.writeFloat(value));
    }

    @Override
    public void writeDouble(double value) throws JMSException {
        run(() -> output.writeDouble(value));
    }

    @Override
    public void writeUTF(String value) throws JMSException {
        run(() -> output.writeUTF(value));
    }

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        run(() -> output.write(value));
    }

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        run(() -> output.write(value, offset, length));
    }

    @Override
    public void writeString(String value) throws JMSException {
        run(() -> output.writeUTF(value));
    }

    @Override
    public void writeObject(Object value) throws JMSException {
        if (value instanceof Byte) {
            writeByte((Byte) value);
        } else if (value instanceof Short) {
            writeShort((Short) value);
        } else if (value instanceof Integer) {
            writeInt((Integer) value);
        } else if (value instanceof Long) {
            writeLong((Long) value);
        } else if (value instanceof Float) {
            writeFloat((Float) value);
        } else if (value instanceof Double) {
            writeDouble((Double) value);
        } else if (value instanceof Character) {
            writeChar((Character) value);
        } else {
            throw new JMSException("unexpected type");
        }
    }

    interface IOSupplier<T> {

        T get() throws IOException;
    }

    interface IORunnable {

        void run() throws IOException;
    }
}
