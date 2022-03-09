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

import com.sun.xml.ws.binding.WebServiceFeatureList;
import org.codegeny.jakartron.servlet.Base;
import org.kohsuke.MetaInfServices;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.*;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServiceRef;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@MetaInfServices
public final class JAXWSIntegration implements Extension {

    private final Set<Class<?>> implementorClasses = new HashSet<>();
    private final Map<Object, Consumer<AfterBeanDiscovery>> beans = new HashMap<>();

    public void collectImplementorClass(@Observes @WithAnnotations(WebService.class) ProcessAnnotatedType<?> event) {
        implementorClasses.add(event.getAnnotatedType().getJavaClass());
    }

    public void collectInjectionPoint(@Observes ProcessInjectionPoint<?, ?> event) {
        InjectionPoint injectionPoint = event.getInjectionPoint();
        injectionPoint.getQualifiers().stream().filter(Base.class::isInstance).map(Base.class::cast).findFirst().ifPresent(base -> process(injectionPoint, base));
    }

    private void process(InjectionPoint injectionPoint, Base base) {
        WebServiceRef webServiceRef = injectionPoint.getAnnotated().getAnnotation(WebServiceRef.class);
        if (webServiceRef != null) {
            Type portType = injectionPoint.getType();
            if (portType instanceof Class<?>) {
                Class<?> portInterface = (Class<?>) portType;
                if (portInterface.isInterface() && portInterface.isAnnotationPresent(WebService.class)) {
                    WebServiceFeatureList webServiceFeatures = new WebServiceFeatureList();
                    webServiceFeatures.parseAnnotations(injectionPoint.getAnnotated().getAnnotations());
                    // TODO improve the key
                    beans.put(new HashSet<>(Arrays.asList(portInterface, base, webServiceRef)), afterBeanDiscovery -> afterBeanDiscovery
                            .addBean()
                            .types(portType)
                            .qualifiers(base)
                            .produceWith(instance -> createPort(instance.select(String.class, base).get(), portInterface, webServiceRef, webServiceFeatures.toArray()))
                    );
                }
            }
        }
    }

    private Object createPort(String uri, Class<?> portInterface, WebServiceRef webServiceRef, WebServiceFeature... features) {
        Object port = createPort(portInterface, webServiceRef, features);
        BindingProvider provider = (BindingProvider) port;
        provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, uri);
        return port;
    }

    private Object createPort(Class<?> portClass, WebServiceRef webServiceRef, WebServiceFeature... features) {
        WebService webService = portClass.getAnnotation(WebService.class);
        String wsdlLocation = webServiceRef.wsdlLocation().isEmpty() ? webService.wsdlLocation() : webServiceRef.wsdlLocation();

        if (webServiceRef.value().equals(Service.class)) {
            if (webService.targetNamespace().isEmpty() || webService.portName().isEmpty() || webService.serviceName().isEmpty()) {
                throw new CreationException("If no Service class is specified in @WebServiceRef.value(), then targetNamespace(), portName() and serviceName() must all 3 be filled in the @WebService annotation on the port interface");
            }
            QName serviceName = new QName(webService.targetNamespace(), webService.serviceName());
            QName portName = new QName(webService.targetNamespace(), webService.portName());
            if (wsdlLocation.isEmpty()) {
                throw new CreationException("If no Service class is specified in @WebServiceRef.value(), then wsdlLocation() must be filled either on @WebServiceRef or on the port interface @WebService");
            }
            Service service = Service.create(Thread.currentThread().getContextClassLoader().getResource(wsdlLocation), serviceName, features);
            return service.getPort(portName, portClass);
        } else {
            try {
                Service service = createService(webServiceRef.value(), wsdlLocation, features);

                Method method = Stream.of(webServiceRef.value().getDeclaredMethods())
                        .filter(m -> m.getParameterCount() == 0 && m.getReturnType().equals(portClass))
                        .findFirst()
                        .orElseThrow(() -> new CreationException("Cannot find a correct port factory method on the given service"));

                return method.invoke(service);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
                throw new CreationException(exception);
            }
        }
    }

    private Service createService(Class<? extends Service> serviceClass, String wsdlLocation, WebServiceFeature... features) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        if (wsdlLocation.isEmpty()) {
            return features.length == 0
                    ? serviceClass.newInstance()
                    : serviceClass.getConstructor(WebServiceFeature[].class).newInstance((Object) features);
        } else {
            URL wsdlLocationUrl = Thread.currentThread().getContextClassLoader().getResource(wsdlLocation);
            return features.length == 0
                    ? serviceClass.getConstructor(URL.class).newInstance(wsdlLocationUrl)
                    : serviceClass.getConstructor(URL.class, WebServiceFeature[].class).newInstance(wsdlLocationUrl, features);
        }
    }

    public void createBeans(@Observes AfterBeanDiscovery event) {
        beans.values().forEach(bean -> bean.accept(event));
    }

    public Set<Class<?>> getImplementorClasses() {
        return implementorClasses;
    }
}
