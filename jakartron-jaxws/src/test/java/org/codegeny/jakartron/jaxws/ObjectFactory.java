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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _EchoResponse_QNAME = new QName("urn:echo", "echoResponse");
    private final static QName _EchoRequest_QNAME = new QName("urn:echo", "echoRequest");

    public EchoRequest createEchoRequest() {
        return new EchoRequest();
    }

    public EchoResponse createEchoResponse() {
        return new EchoResponse();
    }

    @XmlElementDecl(namespace = "urn:echo", name = "echoResponse")
    public JAXBElement<EchoResponse> createEchoResponse(EchoResponse value) {
        return new JAXBElement<>(_EchoResponse_QNAME, EchoResponse.class, null, value);
    }

    @XmlElementDecl(namespace = "urn:echo", name = "echoRequest")
    public JAXBElement<EchoRequest> createEchoRequest(EchoRequest value) {
        return new JAXBElement<>(_EchoRequest_QNAME, EchoRequest.class, null, value);
    }
}
