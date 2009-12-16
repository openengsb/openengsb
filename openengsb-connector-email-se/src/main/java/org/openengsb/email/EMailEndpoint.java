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
package org.openengsb.email;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.methodcalltransformation.InvocationFailedException;
import org.openengsb.core.methodcalltransformation.MethodCall;
import org.openengsb.core.methodcalltransformation.MethodCallTransformer;
import org.openengsb.core.methodcalltransformation.ReturnValue;
import org.openengsb.core.methodcalltransformation.ReturnValueTransformer;
import org.openengsb.util.serialization.SerializationException;

/**
 * @org.apache.xbean.XBean element="emailEndpoint"
 *                         description="EMail Notification Endpoint"
 */
public class EMailEndpoint extends ProviderEndpoint {

    private EMailNotifier emailNotifier = new EMailNotifier();

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        QName operation = exchange.getOperation();
        if (operation != null && operation.getLocalPart().equals("methodcall")) {
            handleMethodCall(in, out);
            return;
        }
    }

    private void handleMethodCall(NormalizedMessage in, NormalizedMessage out) throws TransformerException,
            SerializationException, InvocationFailedException, MessagingException {
        String inXml = new SourceTransformer().toString(in.getContent());
        Segment inSegment = Segment.fromXML(inXml);
        MethodCall methodCall = MethodCallTransformer.transform(inSegment);

        ReturnValue returnValue = methodCall.invoke(emailNotifier);

        Segment returnValueSegment = ReturnValueTransformer.transform(returnValue);
        String returnValueXml = returnValueSegment.toXML();
        out.setContent(new StringSource(returnValueXml));
    }

}
