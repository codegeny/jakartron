package org.codegeny.jakartron.selenium;

/*-
 * #%L
 * jakartron-selenium
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

import org.codegeny.jakartron.servlet.Base;
import org.kohsuke.MetaInfServices;
import org.openqa.selenium.WebDriver;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public class PageExtension implements Extension {

    private static class PageLiteral extends AnnotationLiteral<Page> implements Page {

        private final String value;

        public PageLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    private static class BaseLiteral extends AnnotationLiteral<Base> implements Base {

        private final String value;

        public BaseLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    private final Set<Class<?>> pageClasses = new HashSet<>();

    public void registerPageClass(@Observes @WithAnnotations(Page.class) ProcessAnnotatedType<?> event) {
        event.getAnnotatedType().getFields().stream()
                .filter(f -> f.isAnnotationPresent(Page.class) && f.isAnnotationPresent(Inject.class))
                .map(f -> f.getJavaMember().getType())
                .forEach(pageClasses::add);
        event.getAnnotatedType().getMethods().stream()
                .flatMap(m -> m.getParameters().stream())
                .filter(f -> f.isAnnotationPresent(Page.class))
                .map(f -> f.getJavaParameter().getType())
                .forEach(pageClasses::add);
    }

    public void registerPageBean(@Observes AfterBeanDiscovery event) {
        pageClasses.forEach(pageClass -> event.addBean()
                .types(pageClass)
                .qualifiers(new PageLiteral("dummy"))
                .scope(Dependent.class)
                .produceWith(instance -> produce(pageClass, instance))
        );
    }

    private Object produce(Class<?> pageClass, Instance<Object> instance) {
        InjectionPoint injectionPoint = instance.select(InjectionPoint.class).get();
        String url = injectionPoint.getQualifiers().stream().filter(Page.class::isInstance).map(Page.class::cast).findFirst().orElseThrow(InternalError::new).value();
        WebDriver driver = instance.select(WebDriver.class, new BaseLiteral(url)).get();
        return FragmentablePageFactory.createPage(driver, pageClass);
    }
}
