/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.core;

import java.util.UUID;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.methodcalltransformation.InvocationFailedException;
import org.openengsb.core.methodcalltransformation.MethodCall;
import org.openengsb.core.methodcalltransformation.ReturnValue;
import org.openengsb.core.methodcalltransformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public abstract class OpenEngSBEndpoint<T> extends ProviderEndpoint {

    protected abstract T getImplementation(ContextHelper contextHelper);

    protected QName getForwardTarget(ContextHelper contextHelper) {
        return null;
    }

    protected abstract void inOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper) throws Exception;

    public OpenEngSBEndpoint() {
    }

    public OpenEngSBEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public OpenEngSBEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    @Override
    protected final void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        String contextId = getContextId(in);
        ContextHelper contextHelper = new ContextHelperImpl(this, contextId);

        QName operation = exchange.getOperation();
        if (operation != null && operation.getLocalPart().equals("methodcall")) {
            handleMethodCall(exchange, in, out, contextHelper);
            return;
        }

        inOut(exchange, in, out, contextHelper);
    }

    private String getContextId(NormalizedMessage in) {
        return (String) in.getProperty("contextId");
    }

    // extend the visibility of this method from protected to public
    @Override
    public void sendSync(MessageExchange me) throws MessagingException {
        super.sendSync(me);
    }

    // extend the visibility of this method from protected to public
    @Override
    public void send(MessageExchange me) throws MessagingException {
        super.send(me);
    }

    private void handleMethodCall(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper) throws TransformerException, SerializationException,
            InvocationFailedException, MessagingException {
        MethodCall methodCall = toMethodCall(in.getContent());
        T implementation = getImplementation(contextHelper);
        if (implementation == null) {
            QName forwardTarget = getForwardTarget(contextHelper);
            if (forwardTarget == null) {
                throw new IllegalStateException(
                        "Neither implementation given for method call, nor forward target specified.");
            }
            forwardMessage(exchange, in, out, forwardTarget);
            return;
        }

        ReturnValue returnValue = methodCall.invoke(implementation);

        out.setContent(toSource(returnValue));
    }

    protected MethodCall toMethodCall(Source source) throws SerializationException, TransformerException {
        return Transformer.toMethodCall(new SourceTransformer().toString(source));
    }

    protected ReturnValue toReturnValue(Source source) throws SerializationException, TransformerException {
        return Transformer.toReturnValue(new SourceTransformer().toString(source));
    }

    protected Source toSource(ReturnValue returnValue) throws SerializationException {
        return new StringSource(Transformer.toXml(returnValue));
    }

    protected Source toSource(MethodCall methodCall) throws SerializationException {
        return new StringSource(Transformer.toXml(methodCall));
    }

    protected void forwardMessage(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out, QName service)
            throws MessagingException {
        InOut inout = new InOutImpl(UUID.randomUUID().toString());
        inout.setService(service);
        inout.setInMessage(in);
        inout.setOperation(exchange.getOperation());

        sendSync(inout);

        NormalizedMessage outMessage = inout.getOutMessage();
        out.setContent(outMessage.getContent());
    }
}
