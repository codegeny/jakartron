package org.codegeny.jakartron.jaxws;

/*-
 * #%L
 * jakartron-jaxws
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

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;
import com.sun.xml.ws.util.xml.XmlUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import javax.xml.namespace.QName;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Dependent
public final class JAXWSProducer {

    private void configureEndpoints(@Observes @Initialized(ApplicationScoped.class) ServletContext context, BeanManager beanManager) {
        Dynamic registration = context.addServlet(WSServlet.class.getName(), WSServlet.class);

        List<ServletAdapter> adapters = beanManager.getExtension(JAXWSIntegration.class).getImplementorClasses().stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .map(implementorClass -> toServletAdapter(implementorClass, context, beanManager))
                .peek(adapter -> registration.addMapping(adapter.getValidPath()))
                .collect(Collectors.toList());

        context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO, new WSServletDelegate(adapters, context));
    }

    private ServletAdapter toServletAdapter(Class<?> implementorClass, ServletContext context, BeanManager beanManager) {
        WebService webService = implementorClass.getAnnotation(WebService.class);

        // TODO Allow customization of url
        String urlPattern = "/" + webService.name();

        WSEndpoint<?> endpoint = WSEndpoint.create(
                implementorClass,
                true, // TODO reimplement handlers processing (so they can be managed by CDI)
                new BeanInvoker(beanManager),
                new QName(webService.targetNamespace(), webService.serviceName()),
                new QName(webService.targetNamespace(), webService.portName()),
                createContainer(context),
                createBinding(implementorClass),
                createWsdl(webService.wsdlLocation()),
                Collections.emptyList(),
                XmlUtil.createDefaultCatalogResolver(),
                false
        );

        return new ServletAdapterList(context).createAdapter(webService.name(), urlPattern, endpoint);
    }

    private static SDDocumentSource createWsdl(String wsdlLocation) {
        return wsdlLocation.isEmpty() ? null : SDDocumentSource.create(Thread.currentThread().getContextClassLoader().getResource(wsdlLocation));
    }

    private static Container createContainer(ServletContext context) {
        return new ServletContainer(context, new ServletContextModule(context), new ServletContextResourceLoader(context));
    }

    private static WSBinding createBinding(Class<?> implementorClass) {
        WebServiceFeatureList features = new WebServiceFeatureList();
        features.parseAnnotations(implementorClass);
        BindingID bindingID = BindingID.parse(implementorClass);
        features.addAll(bindingID.createBuiltinFeatureList());
        return bindingID.createBinding(features.toArray());
    }
}
