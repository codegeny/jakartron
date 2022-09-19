package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import org.codegeny.jakartron.jca.ConfigureResourceAdapter;
import org.kohsuke.MetaInfServices;

import javax.ejb.MessageDriven;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.io.Externalizable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MetaInfServices
public final class MessageDrivenExtension implements Extension {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private final Set<AnnotatedType<?>> messageDrivenBeans = new HashSet<>();

    public void configure(@Observes BeforeBeanDiscovery event) {
        event.addAnnotatedType(ContextDataHolder.class, ContextDataHolder.class.getName());
        event.addAnnotatedType(EJBContextImpl.class, EJBContextImpl.class.getName());
    }

    public void processMessageListeners(@Observes @WithAnnotations(MessageDriven.class) ProcessAnnotatedType<?> event) {
        messageDrivenBeans.add(event.getAnnotatedType());
    }

    public void registerMDB(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.<ConfigureResourceAdapter>addObserverMethod()
                .observedType(ConfigureResourceAdapter.class)
                .notifyWith(e -> messageDrivenBeans.forEach(messageDrivenBean -> {
                    MessageDriven messageDriven = messageDrivenBean.getAnnotation(MessageDriven.class);
                    Class<?> listenerInterface;
                    if (messageDriven.messageListenerInterface().equals(Object.class)) {
                        Set<Class<?>> interfaces = getAllInterfaces(messageDrivenBean.getJavaClass())
                                .distinct()
                                .filter(i -> !i.getPackage().getName().startsWith("javax.ejb"))
                                .filter(i -> !Arrays.asList(Serializable.class, Externalizable.class).contains(i))
                                .collect(Collectors.toSet());
                        if (interfaces.size() != 1) {
                            throw new RuntimeException("MDB must implement a single interface or specify @MessageDriven.listenerInterface()");
                        }
                        listenerInterface = interfaces.iterator().next();
                    } else {
                        listenerInterface = messageDriven.messageListenerInterface();
                    }

                    Properties properties = new Properties();
                    Stream.of(messageDriven.activationConfig()).forEach(a -> properties.setProperty(a.propertyName(), evaluate(a.propertyValue())));
                    e.getEvent().addMessageEndpoint(listenerInterface, beanManager.createInstance().select(messageDrivenBean.getJavaClass()), properties, messageDrivenBean.getJavaClass());
                }));
    }

    private String evaluate(String value) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = PLACEHOLDER.matcher(value);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, System.getProperty(matcher.group(1)));
        }
        return matcher.appendTail(buffer).toString();
    }

    private Stream<Class<?>> getAllInterfaces(Class<?> klass) {
        return klass == null ? Stream.empty() : Stream.<Stream<Class<?>>>of(
                klass.isInterface() ? Stream.of(klass) : Stream.empty(),
                getAllInterfaces(klass.getSuperclass()),
                Stream.of(klass.getInterfaces()).flatMap(this::getAllInterfaces)
        ).flatMap(Function.identity());
    }
}
