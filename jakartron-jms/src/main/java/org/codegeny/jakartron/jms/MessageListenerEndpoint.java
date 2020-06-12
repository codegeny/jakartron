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

import javax.enterprise.inject.Instance;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public class MessageListenerEndpoint<T extends MessageListener> extends TransactionalMessageEndpoint implements MessageListener {

    private final Instance<T> instance;
    private final T delegate;

    public MessageListenerEndpoint(TransactionManager transactionManager, XAResource resource, Instance<T> instance) {
        super(transactionManager, resource);
        this.delegate = (this.instance = instance).get();
    }

    public void onMessage(Message message) {
        delegate.onMessage(message);
    }

    public void release() {
        this.instance.destroy(this.delegate);
    }
}
