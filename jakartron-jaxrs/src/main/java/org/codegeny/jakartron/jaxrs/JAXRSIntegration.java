package org.codegeny.jakartron.jaxrs;

/*-
 * #%L
 * jakartron-jaxrs
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

import org.codegeny.jakartron.servlet.Initialized;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class JAXRSIntegration implements Extension {

    private final Set<Class<? extends Application>> applicationClasses = new HashSet<>();

    public void collectApplications(@Observes @WithAnnotations(ApplicationPath.class) ProcessAnnotatedType<? extends Application> event) {
        applicationClasses.add(event.getAnnotatedType().getJavaClass());
    }

    public void addApplications(@Observes AfterBeanDiscovery event) {
        event.<ServletContextEvent>addObserverMethod()
                .observedType(ServletContextEvent.class)
                .qualifiers(Initialized.Literal.INSTANCE)
                .notifyWith(this::initializeContext);
    }

    private void initializeContext(EventContext<ServletContextEvent> eventContext) {
        ServletContext context = eventContext.getEvent().getServletContext();
        context.setInitParameter("resteasy.injector.factory", CdiInjectorFactory.class.getName());
        context.setInitParameter(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS, "true");
        context.setInitParameter(ResteasyContextParameters.RESTEASY_ROLE_BASED_SECURITY, "true");
        applicationClasses.forEach(application -> configureApplication(context, application));
    }

    private void configureApplication(ServletContext context, Class<? extends Application> applicationClass) {
        String prefix = "/".concat(applicationClass.getAnnotation(ApplicationPath.class).value().replaceAll("^/*|/*$", ""));
        ServletRegistration.Dynamic servlet = context.addServlet("resteasy", HttpServlet30Dispatcher.class);
        servlet.addMapping(prefix.equals("/") ? prefix : prefix.concat("/*"));
        servlet.setInitParameter("javax.ws.rs.Application", applicationClass.getName());
        servlet.setInitParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, prefix);
        servlet.setMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir")));
        servlet.setAsyncSupported(true);
    }
}
