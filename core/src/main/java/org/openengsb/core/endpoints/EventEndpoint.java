package org.openengsb.core.endpoints;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.resource.spi.IllegalStateException;
import javax.xml.namespace.QName;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;

public abstract class EventEndpoint extends OpenEngSBEndpoint {

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        String contextId = getContextId(in);
        ContextHelper contextHelper = new ContextHelperImpl(this, contextId);

        QName operation = exchange.getOperation();
        if (operation == null || !operation.getLocalPart().equals("event")) {
            throw new IllegalStateException("Operation should be event but is " + operation);
        }
        handleEvent(exchange, in, out, contextHelper);
    }

    protected abstract void handleEvent(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper) throws MessagingException;

}
