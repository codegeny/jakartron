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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

@WebService(name = "EchoWebService", targetNamespace = "urn:echo", serviceName = "EchoService", portName = "EchoPort")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso(ObjectFactory.class)
public interface EchoPort {

    @WebMethod
    @WebResult(name = "echoResponse", targetNamespace = "urn:echo", partName = "parameters")
    EchoResponse echo(@WebParam(name = "echoRequest", targetNamespace = "urn:echo", partName = "parameters") EchoRequest parameters);
}
