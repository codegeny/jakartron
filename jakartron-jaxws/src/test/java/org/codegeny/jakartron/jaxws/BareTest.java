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

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.codegeny.jakartron.servlet.Base;
import org.junit.jupiter.api.Test;

import javax.jws.WebService;
import javax.xml.ws.BindingProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWithJakartron
public class BareTest {

    @WebService(
            name = "EchoWebService",
            targetNamespace = "urn:echo",
            serviceName = "EchoService",
            portName = "EchoPort",
            endpointInterface = "org.codegeny.jakartron.jaxws.EchoPort",
            wsdlLocation = "org/codegeny/jakartron/jaxws/echo.wsdl"
    )
    public static class EchoWebService implements EchoPort {

        @Override
        public EchoResponse echo(EchoRequest parameters) {
            EchoResponse response = new EchoResponse();
            response.setMessage("ECHO " + parameters.getMessage());
            return response;
        }
    }

    @Test
    public void test(@Base("EchoWebService") String uri) {
        EchoService service = new EchoService();
        EchoPort port = service.getEchoPort();

        BindingProvider provider = (BindingProvider) port;
        provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, uri);

        EchoRequest request = new EchoRequest();
        request.setMessage("HELLO!!!");

        EchoResponse response = port.echo(request);
        assertEquals("ECHO HELLO!!!", response.getMessage());
    }
}
