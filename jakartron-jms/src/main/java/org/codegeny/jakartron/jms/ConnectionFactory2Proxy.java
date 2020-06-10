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
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.TransactionHelper;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;

public class ConnectionFactory2Proxy extends ConnectionFactoryProxy {

    private final XAConnectionFactory xaConnectionFactory;
    private final TransactionHelper transactionHelper;

    public ConnectionFactory2Proxy(XAConnectionFactory xaConnectionFactory, TransactionHelper transactionHelper) {
        super(xaConnectionFactory, transactionHelper);
        this.xaConnectionFactory = xaConnectionFactory;
        this.transactionHelper = transactionHelper;
    }

    @Override
    public JMSContext createContext() {
        return createAndRegisterContext();
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        return createContext();
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        return createContext();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return createContext();
    }

    private JMSContext createAndRegisterContext() {
        XAJMSContext xaJMSContext = xaConnectionFactory.createXAContext();
        JMSContext context = new JMSContextProxy(xaJMSContext, xaConnectionFactory, transactionHelper);

        try {
            transactionHelper.registerXAResource(xaJMSContext.getXAResource());
        } catch (RuntimeException e) {
            xaJMSContext.close();
            throw e;
        } catch (JMSException jmsException) {
            xaJMSContext.close();
            throw new JMSRuntimeException(jmsException.getMessage(), jmsException.getErrorCode(), jmsException.getCause());
        }

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Created new proxied context: " + xaJMSContext);
        }

        return context;
    }
}
