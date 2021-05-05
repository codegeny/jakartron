package org.codegeny.jakartron.jsf;

/*-
 * #%L
 * jakartron-jsf
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

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FragmentablePageFactory {

    public static void initPage(WebDriver driver, Object pageObject) {
        PageFactory.initElements(new FragmentDefaultFieldDecorator(driver, driver), pageObject);
    }

    public static <T> T createPage(WebDriver driver, Class<T> pageClass) {
        T page = instantiate(pageClass, driver);
        initPage(driver, page);
        return page;
    }

    private static final class FragmentDefaultFieldDecorator extends DefaultFieldDecorator {

        private final WebDriver driver;

        FragmentDefaultFieldDecorator(WebDriver driver, SearchContext context) {
            super(new DefaultElementLocatorFactory(context));
            this.driver = driver;
        }

        @Override
        public Object decorate(ClassLoader loader, Field field) {
            Object result = super.decorate(loader, field);
            if (result == null && field.isAnnotationPresent(FindBy.class)) {
                if (List.class.equals(field.getType())) {
                    List<WebElement> webElements = super.factory.createLocator(field).findElements();
                    if (webElements != null) {
                        Class<?> type = (Class<?>) (((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
                        result = webElements.stream().map(webElement -> createFragment(webElement, type)).collect(Collectors.toList());
                    }
                } else {
                    WebElement webElement = super.factory.createLocator(field).findElement();
                    if (webElement != null) {
                        result = createFragment(webElement, field.getType());
                    }
                }
            }
            return result;
        }

        private <T> T createFragment(WebElement webElement, Class<T> fragmentClass) {
            T fragment = instantiate(fragmentClass, driver, webElement);
            PageFactory.initElements(new FragmentDefaultFieldDecorator(driver, webElement), fragment);
            return fragment;
        }
    }

    // find the best constructor for the given parameters (matching by type)
    private static <T> T instantiate(Class<T> type, Object... parameters) {
        return Stream.of(type.getConstructors())
                .sorted(Comparator.<Constructor<?>>comparingInt(Constructor::getParameterCount).reversed())
                .filter(c -> Stream.of(c.getParameterTypes()).allMatch(t -> Stream.of(parameters).anyMatch(t::isInstance)))
                .findFirst()
                .map(c -> instantiate(c, Stream.of(c.getParameterTypes()).map(t -> Stream.of(parameters).filter(t::isInstance).findFirst().orElseThrow(InternalError::new)).toArray()))
                .map(type::cast)
                .orElseThrow(() -> new IllegalStateException(String.format("Can't find supported constructor for %s and parameters %s", type, Arrays.asList(parameters))));
    }

    private static <T> T instantiate(Constructor<T> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }
}
