package org.codegeny.jakartron.servlet;

/*-
 * #%L
 * jakartron-servlet
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

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.module.web.servlet.WeldInitialListener;
import org.jboss.weld.module.web.servlet.WeldTerminalListener;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Simple Servlet integration which starts/stops a jetty server on a random available port and fire a ServletContext
 * event for dynamic registration of servlets, filters, etc. The base URI of the server is also published as a bean and
 * can be retrieved through regular injection.
 */
final class ServletProducer {

    private static final Logger LOGGER = Logger.getLogger(ServletProducer.class.getName());

    @Produces
    @Base
    public URI uri(InjectionPoint injectionPoint, Server server) {
        return injectionPoint.getQualifiers().stream()
                .filter(Base.class::isInstance)
                .map(Base.class::cast)
                .map(Base::value)
                .map(server.getURI()::resolve)
                .findFirst()
                .orElseThrow(InternalError::new);
    }

    @Produces
    @Base
    public String string(InjectionPoint injectionPoint, Server server) {
        return uri(injectionPoint, server).toASCIIString();
    }

    @Produces
    @Base
    public URL url(InjectionPoint injectionPoint, Server server) throws MalformedURLException {
        return uri(injectionPoint, server).toURL();
    }

    @Produces
    @Singleton
    private Server startServer(WebAppContext webAppContext) throws Exception {
        Server server = new Server(0);
        Configuration.ClassList.setServerDefault(server)
                .addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
        server.setHandler(webAppContext);
        server.start();
        LOGGER.info(() -> String.format("Started server on %s", server.getURI()));
        return server;
    }

    @Produces
    private WebAppContext webAppContext(BeanManager beanManager, BridgingServletContextListener listener, LoginService loginService) throws Exception {
        //WebAppContext webAppContext = new WebAppContext(Resource.newClassPathResource("META-INF/resources"), "/");
        WebAppContext webAppContext = new WebAppContext(System.getProperty("java.io.tmpdir"), "/");
        //webAppContext.setBaseResource(Resource.newClassPathResource("META-INF/resources"));
        webAppContext.setBaseResource(Resource.newClassPathResource("/"));
        webAppContext.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*taglibs-standard-impl-.*\\.jar$");
        webAppContext.addEventListener(new WeldInitialListener(BeanManagerProxy.unwrap(beanManager)));
        webAppContext.addEventListener(listener);
        webAppContext.addEventListener(new WeldTerminalListener(BeanManagerProxy.unwrap(beanManager)));
        webAppContext.getSecurityHandler().setLoginService(loginService);
        webAppContext.configure();
        return webAppContext;
    }

    @Produces
    public LoginService loginService(UserStore userStore) {
        HashLoginService loginService = new HashLoginService();
        loginService.setUserStore(userStore);
        return loginService;
    }

    @Produces
    public UserStore userStore(Event<SecurityConfigurationEvent> event) {
        UserStore userStore = new UserStore();
        event.fire((name, credentials, roles) -> userStore.addUser(name, new PlainCredentials(credentials), roles));
        return userStore;
    }

    public void stopServer(@Disposes Server server) throws Exception {
        server.stop();
    }

    private static final class PlainCredentials extends Credential {

        private final String password;

        PlainCredentials(String password) {
            this.password = password;
        }

        @Override
        public boolean check(Object credentials) {
            return password.equals(credentials);
        }
    }
}
