package org.codegeny.jakartron.jmsra;

import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivationSpec;
import org.codegeny.jakartron.jca.ConfigureResourceAdapter;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.jms.MessageListener;

@Dependent
public class JMSRAProducer {

    public void registerAdapter(@Observes ConfigureResourceAdapter event, ActiveMQServer server) {

        server.getConnectorsService().getConnectors().forEach((k, v) -> System.out.printf("%s=%s%n", k, v));


        ActiveMQResourceAdapter resourceAdapter = new ActiveMQResourceAdapter();
        resourceAdapter.setConnectorClassName(InVMConnectorFactory.class.getName());
        resourceAdapter.setConnectionParameters(String.format("%s=%s", TransportConstants.SERVER_ID_PROP_NAME, server.getIdentity()));
        event.setResourceAdapter(MessageListener.class, resourceAdapter, ActiveMQActivationSpec.class);
    }
}
