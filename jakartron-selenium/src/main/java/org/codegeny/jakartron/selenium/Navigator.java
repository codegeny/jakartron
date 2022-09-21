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
import org.openqa.selenium.WebDriver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.stream.Stream;

@ApplicationScoped
public class Navigator {

    @Inject
    @Base
    private URI uri;

    @Inject
    private WebDriver webDriver;

    public <T> T current(Class<T> pageClass) {
        return FragmentablePageFactory.createPage(webDriver, pageClass);
    }

    public <T> T navigateTo(Class<T> pageClass, String... parameters) {
        Location location = pageClass.getAnnotation(Location.class);
        if (location == null) {
            throw new IllegalStateException(pageClass + " is not annotated with @Location");
        }
        return navigateToPath(pageClass, location.value(), parameters);
    }

    public <T> T navigateToPath(Class<T> pageClass, String path, String... parameters) {
        webDriver.get(uri.resolve(String.format(path, Stream.of(parameters).map(Navigator::encode).toArray())).toASCIIString());
        return current(pageClass);
    }

    private static String encode(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
