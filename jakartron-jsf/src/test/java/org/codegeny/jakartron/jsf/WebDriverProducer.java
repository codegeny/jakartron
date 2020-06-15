package org.codegeny.jakartron.jsf;

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
