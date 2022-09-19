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

import org.codegeny.jakartron.AdditionalClasses;
import org.codegeny.jakartron.jaxws.events.*;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.codegeny.jakartron.servlet.Base;
import org.junit.jupiter.api.Test;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jws.*;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceRef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWithJakartron
@AdditionalClasses(EventHandler.class)
public class HandlerTest {

    private static final String NAMESPACE = "urn:echo";

    @WebService(name = "EchoWebService", targetNamespace = NAMESPACE, serviceName = "EchoService", portName = "EchoPort")
    @HandlerChain(file = "/org/codegeny/jakartron/jaxws/handlers.xml")
    public static class EchoWebService {

        @WebMethod
        @WebResult(name = "message")
        @RequestWrapper(localName = "echoRequest", targetNamespace = NAMESPACE)
        @ResponseWrapper(localName = "echoResponse", targetNamespace = NAMESPACE)
        public String echo(@WebParam(name = "message") String message) {
            return "ECHO " + message;
        }
    }

    @Inject
    @Base("EchoWebService")
    @WebServiceRef(EchoService.class)
    private EchoPort port;

    private boolean inbound;

    private boolean outbound;

    private boolean filtered;

    private boolean other;

    @Test
    public void test() {
        EchoRequest request = new EchoRequest();
        request.setMessage("HELLO!!!");
        EchoResponse response = port.echo(request);
        assertEquals("ECHO HELLO!!!", response.getMessage());
        assertTrue(inbound & outbound & filtered & !other);
    }

    public void filtered(@Observes @Inbound @ServiceName("{urn:echo}EchoService") SOAPEvent context) {
        filtered = true;
    }

    public void other(@Observes @Inbound @ServiceName("{urn:other}OtherService") SOAPEvent context) {
        other = true;
    }

    public void inbound(@Observes @Inbound SOAPEvent context) {
        inbound = true;
    }

    public void outbound(@Observes @Outbound SOAPEvent context) {
        outbound = true;
    }
}
