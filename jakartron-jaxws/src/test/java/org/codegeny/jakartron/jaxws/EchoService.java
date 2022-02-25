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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.URL;

@WebServiceClient(name = "EchoService", targetNamespace = "urn:echo", wsdlLocation = "org/codegeny/jakartron/jaxws/echo.wsdl")
public class EchoService extends Service {

    private final static URL WSDL_LOCATION;
    private final static QName SERVICE_QNAME = new QName("urn:echo", "EchoService");
    private final static QName PORT_QNAME = new QName("urn:echo", "EchoPort");

    static {
        WSDL_LOCATION = Thread.currentThread().getContextClassLoader().getResource("org/codegeny/jakartron/jaxws/echo.wsdl");
    }

    public EchoService() {
        super(WSDL_LOCATION, SERVICE_QNAME);
    }

    public EchoService(WebServiceFeature... features) {
        super(WSDL_LOCATION, SERVICE_QNAME, features);
    }

    public EchoService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE_QNAME);
    }

    public EchoService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SERVICE_QNAME, features);
    }

    public EchoService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EchoService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    @WebEndpoint(name = "EchoPort")
    public EchoPort getEchoPort() {
        return super.getPort(PORT_QNAME, EchoPort.class);
    }

    @WebEndpoint(name = "EchoPort")
    public EchoPort getEchoPort(WebServiceFeature... features) {
        return super.getPort(PORT_QNAME, EchoPort.class, features);
    }
}
