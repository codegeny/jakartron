package org.codegeny.jakartron.jaxrs;

/*-
 * #%L
 * jakartron-jaxrs
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

import org.codegeny.jakartron.Internal;
import org.codegeny.jakartron.servlet.Base;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.lang.annotation.Annotation;
import java.net.URI;

@Dependent
public final class JAXRSProducer {

    @Produces
    @ApplicationScoped
    @Internal
    public Client newClient() {
        return ClientBuilder.newClient();
    }

    public void closeClient(@Disposes @Internal Client client) {
        client.close();
    }

    @Produces
    @Base
    public WebTarget newWebTarget(@Internal Client client, InjectionPoint injectionPoint, @Any Instance<URI> uriProvider) {
        return client.target(uriProvider.select(injectionPoint.getQualifiers().toArray(new Annotation[0])).get());
    }

    @Produces
    @Base
    public SseEventSource newSseEventSource(InjectionPoint injectionPoint, @Any Instance<WebTarget> targetProvider) {
        return SseEventSource.target(targetProvider.select(injectionPoint.getQualifiers().toArray(new Annotation[0])).get()).build();
    }

    public void closeSseEventSource(@Disposes @Base SseEventSource sseEventSource) {
        if (sseEventSource.isOpen()) {
            sseEventSource.close();
        }
    }
}
