package org.codegeny.jakartron.properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.util.Map;
import java.util.Properties;

public class PropertiesProducer {

    @Produces
    @Named("systemProperties")
    @ApplicationScoped
    public Properties systemProperties() {
        return System.getProperties();
    }

    @Produces
    @Named("environmentVariables")
    @ApplicationScoped
    public Map<String, String> environmentVariables() {
        return System.getenv();
    }
}
