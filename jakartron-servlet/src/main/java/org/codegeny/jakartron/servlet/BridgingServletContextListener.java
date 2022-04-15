package org.codegeny.jakartron.servlet;

/*-
 * #%L
 * jakartron-servlet
 * %%
 * Copyright (C) 2018 - 2021 Codegeny
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

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.*;
import javax.servlet.http.*;

final class BridgingServletContextListener implements ServletContextListener, ServletContextAttributeListener, ServletRequestListener, ServletRequestAttributeListener, HttpSessionListener, HttpSessionAttributeListener, HttpSessionActivationListener, HttpSessionBindingListener {

    private final BeanManager beanManager;

    public BridgingServletContextListener(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        beanManager.getEvent().select(ServletContextEvent.class, Initialized.Literal.INSTANCE).fire(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        beanManager.getEvent().select(ServletContextEvent.class, Destroyed.Literal.INSTANCE).fire(servletContextEvent);
    }

    @Override
    public void attributeAdded(ServletRequestAttributeEvent servletRequestAttributeEvent) {
        beanManager.getEvent().select(ServletRequestAttributeEvent.class, Added.Literal.INSTANCE).fire(servletRequestAttributeEvent);
    }

    @Override
    public void attributeRemoved(ServletRequestAttributeEvent servletRequestAttributeEvent) {
        beanManager.getEvent().select(ServletRequestAttributeEvent.class, Removed.Literal.INSTANCE).fire(servletRequestAttributeEvent);
    }

    @Override
    public void attributeReplaced(ServletRequestAttributeEvent servletRequestAttributeEvent) {
        beanManager.getEvent().select(ServletRequestAttributeEvent.class, Replaced.Literal.INSTANCE).fire(servletRequestAttributeEvent);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        beanManager.getEvent().select(ServletRequestEvent.class, Destroyed.Literal.INSTANCE).fire(servletRequestEvent);
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        beanManager.getEvent().select(ServletRequestEvent.class, Initialized.Literal.INSTANCE).fire(servletRequestEvent);
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent httpSessionEvent) {
        beanManager.getEvent().select(HttpSessionEvent.class, PrePassivation.Literal.INSTANCE).fire(httpSessionEvent);
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent httpSessionEvent) {
        beanManager.getEvent().select(HttpSessionEvent.class, PostActivation.Literal.INSTANCE).fire(httpSessionEvent);
    }

    @Override
    public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent) {
        beanManager.getEvent().select(HttpSessionBindingEvent.class, Bound.Literal.INSTANCE).fire(httpSessionBindingEvent);
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent) {
        beanManager.getEvent().select(HttpSessionBindingEvent.class, Unbound.Literal.INSTANCE).fire(httpSessionBindingEvent);
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        beanManager.getEvent().select(HttpSessionEvent.class, Created.Literal.INSTANCE).fire(httpSessionEvent);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        beanManager.getEvent().select(HttpSessionEvent.class, Destroyed.Literal.INSTANCE).fire(httpSessionEvent);
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        beanManager.getEvent().select(HttpSessionBindingEvent.class, Added.Literal.INSTANCE).fire(httpSessionBindingEvent);
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        beanManager.getEvent().select(HttpSessionBindingEvent.class, Removed.Literal.INSTANCE).fire(httpSessionBindingEvent);
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        beanManager.getEvent().select(HttpSessionBindingEvent.class, Replaced.Literal.INSTANCE).fire(httpSessionBindingEvent);
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent servletContextAttributeEvent) {
        beanManager.getEvent().select(ServletContextAttributeEvent.class, Added.Literal.INSTANCE).fire(servletContextAttributeEvent);
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent servletContextAttributeEvent) {
        beanManager.getEvent().select(ServletContextAttributeEvent.class, Removed.Literal.INSTANCE).fire(servletContextAttributeEvent);
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent servletContextAttributeEvent) {
        beanManager.getEvent().select(ServletContextAttributeEvent.class, Replaced.Literal.INSTANCE).fire(servletContextAttributeEvent);
    }
}
