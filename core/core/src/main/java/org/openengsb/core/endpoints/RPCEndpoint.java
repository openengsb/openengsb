/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.endpoints;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.InvocationFailedException;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.util.serialization.SerializationException;

public abstract class RPCEndpoint<T> extends OpenEngSBEndpoint {

    protected abstract T getImplementation(ContextHelper contextHelper, MessageProperties msgProperties);

    protected abstract QName getForwardTarget(ContextHelper contextHelper);

    protected abstract boolean handleCallAutomatically();

    protected abstract void handleMethodCallManually(MessageExchange exchange, NormalizedMessage in,
            NormalizedMessage out, ContextHelper contextHelper, MessageProperties msgProperties) throws Exception;

    public RPCEndpoint() {
    }

    public RPCEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public RPCEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    @Override
    protected final void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        MessageProperties msgProperties = readProperties(in);
        ContextHelper contextHelper = new ContextHelperImpl(this, msgProperties);

        QName operation = exchange.getOperation();
        if (operation == null || !operation.getLocalPart().equals("methodcall")) {
            throw new IllegalStateException(
                    "'methodcall' expected in opertation field of exchange, but '" + operation + "' found.");
        }

        if (handleCallAutomatically()) {
            handleMethodCall(exchange, in, out, contextHelper, msgProperties);
        } else {
            handleMethodCallManually(exchange, in, out, contextHelper, msgProperties);
        }
    }

    private void handleMethodCall(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper, MessageProperties msgProperties) throws TransformerException,
            SerializationException, InvocationFailedException, MessagingException {
        MethodCall methodCall = toMethodCall(in.getContent());

        T implementation = getImplementation(contextHelper, msgProperties);
        if (implementation == null) {
            QName forwardTarget = getForwardTarget(contextHelper);
            if (forwardTarget == null) {
                throw new IllegalStateException(
                        "Neither implementation given for method call, nor forward target specified.");
            }
            forwardInOutMessage(exchange, in, out, forwardTarget);
            return;
        }

        ReturnValue returnValue = methodCall.invoke(implementation);

        out.setContent(toSource(returnValue));

    }

}
