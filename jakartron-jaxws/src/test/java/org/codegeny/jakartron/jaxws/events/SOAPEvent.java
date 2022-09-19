package org.codegeny.jakartron.jaxws.events;

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

import org.xml.sax.InputSource;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;

public interface SOAPEvent {

    enum Direction {

        INBOUND, OUTBOUND
    }

    SOAPMessageContext getContext();

    default QName getServiceName() {
        return (QName) getContext().get(MessageContext.WSDL_SERVICE);
    }

    default QName getPortName() {
        return (QName) getContext().get(MessageContext.WSDL_PORT);
    }

    default QName getOperation() {
        return (QName) getContext().get(MessageContext.WSDL_OPERATION);
    }

    default Direction getDirection() {
        return Boolean.TRUE.equals(getContext().get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) ? Direction.OUTBOUND : Direction.INBOUND;
    }

    @SuppressWarnings("unchecked")
    default Map<String, DataHandler> getInboundAttachments() {
        return (Map<String, DataHandler>) getContext().get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
    }

    @SuppressWarnings("unchecked")
    default Map<String, DataHandler> getOutboundAttachments() {
        return (Map<String, DataHandler>) getContext().get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
    }

    default InputSource getWsdl() {
        return (InputSource) getContext().get(MessageContext.WSDL_DESCRIPTION);
    }

    default QName getInterface() {
        return (QName) getContext().get(MessageContext.WSDL_INTERFACE);
    }
}
