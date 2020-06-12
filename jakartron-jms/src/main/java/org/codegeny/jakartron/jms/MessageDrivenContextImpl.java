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

import org.codegeny.jakartron.jndi.JNDI;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TimerService;
import javax.enterprise.inject.Instance;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.security.Identity;
import java.security.Principal;
import java.util.Map;
import java.util.Properties;

class MessageDrivenContextImpl implements MessageDrivenContext {

    private final Instance<Object> instance;

    public MessageDrivenContextImpl(Instance<Object> instance) {
        this.instance = instance;
    }

    @Override
    public EJBHome getEJBHome() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EJBLocalHome getEJBLocalHome() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getCallerPrincipal() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isCallerInRole(Identity role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCallerInRole(String roleName) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserTransaction getUserTransaction() throws IllegalStateException {
        return instance.select(UserTransaction.class).get();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        try {
            getUserTransaction().setRollbackOnly();
        } catch (SystemException systemException) {
           throw new IllegalStateException(systemException);
        }
    }

    @Override
    public boolean getRollbackOnly() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimerService getTimerService() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object lookup(String name) throws IllegalArgumentException {
        return instance.select(JNDI.Literal.of(name)).get();
    }

    @Override
    public Map<String, Object> getContextData() {
        throw new UnsupportedOperationException();
    }
}
