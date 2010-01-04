package org.openengsb.core.endpoints;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.openengsb.contextcommon.ContextHelper;

public class EventForwardEndpoint extends EventEndpoint {

    @Override
    protected void handleEvent(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper) throws MessagingException {
        String servicename = contextHelper.getValue("event/defaultTarget/servicename");
        String namespace = contextHelper.getValue("event/defaultTarget/namespace");
        QName service = new QName(namespace, servicename);
        forwardMessage(exchange, in, out, service);
    }
}
