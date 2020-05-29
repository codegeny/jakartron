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

import org.kohsuke.MetaInfServices;

import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.servlet.*;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@MetaInfServices
public final class ServletExtension implements Extension {

    private final Set<Class<? extends Servlet>> servlets = new HashSet<>();
    private final Set<Class<? extends Filter>> filters = new HashSet<>();
    private final Map<ServletContainerInitializer, Set<Class<?>>> initializers = new HashMap<>();

    public void createInitializers(@Observes BeforeBeanDiscovery event) {
        StreamSupport.stream(ServiceLoader.load(ServletContainerInitializer.class).spliterator(), false)
                .filter(s -> s.getClass().isAnnotationPresent(HandlesTypes.class))
                .forEach(s -> initializers.put(s, new HashSet<>()));
    }

    public void processInitializer(@Observes ProcessAnnotatedType<?> event) {
        for (Map.Entry<ServletContainerInitializer, Set<Class<?>>> entry : initializers.entrySet()) {
            HandlesTypes handlesTypes = entry.getKey().getClass().getAnnotation(HandlesTypes.class);
            for (Class<?> klass : handlesTypes.value()) {
                if ((klass.isAnnotation() && event.getAnnotatedType().isAnnotationPresent(klass.asSubclass(Annotation.class))) || klass.isAssignableFrom(event.getAnnotatedType().getJavaClass())) {
                    entry.getValue().add(event.getAnnotatedType().getJavaClass());
                }
            }
        }
    }

    public void collectServlets(@Observes @WithAnnotations(WebServlet.class) ProcessAnnotatedType<? extends Servlet> event) {
        servlets.add(event.getAnnotatedType().getJavaClass());
    }

    public void collectFilters(@Observes @WithAnnotations(WebFilter.class) ProcessAnnotatedType<? extends Filter> event) {
        filters.add(event.getAnnotatedType().getJavaClass());
    }

    public void addObserver(@Observes AfterBeanDiscovery event) {
        event.<ServletContext>addObserverMethod()
                .priority(Integer.MIN_VALUE)
                .observedType(ServletContext.class)
                .qualifiers(Initialized.Literal.APPLICATION)
                .notifyWith(e -> register(e.getEvent()));
    }

    private void register(ServletContext servletContext) throws ServletException {
        for (Map.Entry<ServletContainerInitializer, Set<Class<?>>> entry : initializers.entrySet()) {
            entry.getKey().onStartup(entry.getValue(), servletContext);
        }
        servlets.forEach(servlet -> registerServlet(servletContext, servlet));
        filters.forEach(filter -> registerFilter(servletContext, filter));
    }

    private void registerServlet(ServletContext servletContext, Class<? extends Servlet> servlet) {
        WebServlet webServlet = servlet.getAnnotation(WebServlet.class);
        ServletRegistration.Dynamic dynamic = servletContext.addServlet(webServlet.name(), servlet);
        dynamic.addMapping(webServlet.urlPatterns());
        dynamic.setAsyncSupported(webServlet.asyncSupported());
        Stream.of(webServlet.initParams()).forEach(p -> dynamic.setInitParameter(p.name(), p.value()));
    }

    private void registerFilter(ServletContext servletContext, Class<? extends Filter> filter) {
        WebFilter webFilter = filter.getAnnotation(WebFilter.class);
        FilterRegistration.Dynamic dynamic = servletContext.addFilter(webFilter.filterName(), filter);
        dynamic.addMappingForUrlPatterns(EnumSet.copyOf(Arrays.asList(webFilter.dispatcherTypes())), false, webFilter.urlPatterns());
        dynamic.setAsyncSupported(webFilter.asyncSupported());
        Stream.of(webFilter.initParams()).forEach(p -> dynamic.setInitParameter(p.name(), p.value()));
    }
}
