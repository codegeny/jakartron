package org.codegeny.jakartron.jsf;

/*-
 * #%L
 * jakartron-jsf
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

import org.codegeny.jakartron.servlet.Base;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;

@Dependent
public class WebDriverProducer {

    @Produces
    @Base
    public WebDriver driver(InjectionPoint injectionPoint, @Any Instance<String> uriProvider) {
        Annotation base = injectionPoint.getQualifiers().stream().filter(Base.class::isInstance).findFirst().orElseThrow(InternalError::new);
        String uri = uriProvider.select(base).get();
        WebDriver driver = new HtmlUnitDriver();
        driver.get(uri);
        return driver;
    }

    public void closeDriver(@Disposes @Base WebDriver driver) {
        driver.close();
    }
}
