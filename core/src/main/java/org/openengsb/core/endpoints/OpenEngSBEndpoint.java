/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.core.endpoints;

import java.util.UUID;

import javax.jbi.messaging.InOnly;
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
import org.apache.servicemix.jbi.messaging.InOnlyImpl;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.openengsb.core.EventHelper;
import org.openengsb.core.EventHelperImpl;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class OpenEngSBEndpoint extends ProviderEndpoint {

    public OpenEngSBEndpoint() {
    }

    public OpenEngSBEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public OpenEngSBEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
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

    protected String getContextId(NormalizedMessage in) {
        return (String) in.getProperty("contextId");
    }

    protected void forwardInOutMessage(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            QName service) throws MessagingException {
        InOut inout = new InOutImpl(UUID.randomUUID().toString());
        inout.setService(service);
        inout.setInMessage(in);
        inout.setOperation(exchange.getOperation());

        sendSync(inout);

        NormalizedMessage outMessage = inout.getOutMessage();
        out.setContent(outMessage.getContent());
    }

    protected void forwardInOnlyMessage(MessageExchange exchange, NormalizedMessage in, QName service)
            throws MessagingException {
        InOnly inonly = new InOnlyImpl(UUID.randomUUID().toString());
        inonly.setService(service);
        inonly.setInMessage(in);
        inonly.setOperation(exchange.getOperation());

        sendSync(inonly);
    }

    public EventHelper createEventHelper(String contextId) {
        return new EventHelperImpl(this, contextId);
    }
}
