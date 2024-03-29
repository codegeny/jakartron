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

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Set;

@Dependent
public class EventHandler implements SOAPHandler<SOAPMessageContext> {

    @Inject
    @Inbound
    private Event<SOAPEvent> inboundEvent;

    @Inject
    @Outbound
    private Event<SOAPEvent> outboundEvent;

    @Inject
    @Fault
    private Event<SOAPEvent> faultEvent;

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = Boolean.TRUE.equals(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        QName serviceName = (QName) context.get(MessageContext.WSDL_SERVICE);
        QName portName = (QName) context.get(MessageContext.WSDL_PORT);
        QName operation = (QName) context.get(MessageContext.WSDL_OPERATION);
        (outbound ? outboundEvent : inboundEvent).select(
                new ServiceName.Literal(serviceName.toString()),
                new PortName.Literal(portName.toString()),
                new Operation.Literal(operation.toString())
        ).fire(() -> context);
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        faultEvent.fire(() -> context);
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }
}
