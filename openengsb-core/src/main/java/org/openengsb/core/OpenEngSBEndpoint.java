package org.openengsb.core;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.methodcalltransformation.InvocationFailedException;
import org.openengsb.core.methodcalltransformation.MethodCall;
import org.openengsb.core.methodcalltransformation.ReturnValue;
import org.openengsb.core.methodcalltransformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public abstract class OpenEngSBEndpoint<T> extends ProviderEndpoint {

    protected abstract T getImplementation();

    protected abstract void inOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception;

    @Override
    protected final void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        QName operation = exchange.getOperation();
        if (operation != null && operation.getLocalPart().equals("methodcall")) {
            handleMethodCall(in, out);
            return;
        }

        inOut(exchange, in, out);
    }

    private void handleMethodCall(NormalizedMessage in, NormalizedMessage out) throws TransformerException,
            SerializationException, InvocationFailedException, MessagingException {
        MethodCall methodCall = toMethodCall(in.getContent());

        ReturnValue returnValue = methodCall.invoke(getImplementation());

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
}
