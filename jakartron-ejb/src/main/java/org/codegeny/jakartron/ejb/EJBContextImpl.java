package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import javax.ejb.EJBContext;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;
import java.security.Principal;
import java.util.Map;
import java.util.Properties;

@Dependent
public class EJBContextImpl implements EJBContext, SessionContext, MessageDrivenContext {

    @Inject
    private Instance<Object> instance;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private ContextDataHolder contextDataHolder;

    @Override
    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EJBObject getEJBObject() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageContext getMessageContext() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getInvokedBusinessInterface() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean wasCancelCalled() throws IllegalStateException {
        return false;
    }

    @Override
    public EJBHome getEJBHome() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EJBLocalHome getEJBLocalHome() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public java.security.Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getCallerPrincipal() throws IllegalStateException {
        return securityContext.getCallerPrincipal();
    }

    @Deprecated
    @Override
    public boolean isCallerInRole(java.security.Identity role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCallerInRole(String roleName) throws IllegalStateException {
        return securityContext.isCallerInRole(roleName);
    }

    @Override
    public UserTransaction getUserTransaction() throws IllegalStateException {
        return userTransaction;
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        try {
            userTransaction.setRollbackOnly();
        } catch (SystemException systemException) {
            throw new IllegalStateException(systemException);
        }
    }

    @Override
    public boolean getRollbackOnly() throws IllegalStateException {
        try {
            return userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException systemException) {
            throw new IllegalStateException(systemException);
        }
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
        return contextDataHolder.getContextData();
    }
}
