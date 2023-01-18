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

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

@Priority(Interceptor.Priority.APPLICATION)
@Dependent
@Interceptor
@BasicLogin(name = "dummy", password = "dummy")
public class BasicLoginInterceptor {

    @Inject
    @Base
    private URI uri;

    @Inject
    private WebDriver webDriver;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Stream.of(context.getMethod(), context.getMethod().getDeclaringClass())
                .filter(annotated -> annotated.isAnnotationPresent(BasicLogin.class))
                .map(annotated -> annotated.getAnnotation(BasicLogin.class))
                .findFirst()
                .ifPresent(this::basicLogin);
        return context.proceed();
    }

    private void basicLogin(BasicLogin basicLogin) {
        try {
            URI loginURI = new URI(
                    uri.getScheme(),
                    basicLogin.name() + ":" + basicLogin.password(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );
            webDriver.get(loginURI.toASCIIString());
        } catch (URISyntaxException uriSyntaxException) {
            throw new RuntimeException(uriSyntaxException);
        }
    }
}
