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
package org.openengsb.core;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class EventHelperImpl implements EventHelper {

    private ContextHelper contextHelper;

    private MessageProperties msgProperties;

    private OpenEngSBEndpoint endpoint;

    public EventHelperImpl(OpenEngSBEndpoint endpoint, MessageProperties msgProperties) {
        this.endpoint = endpoint;
        this.msgProperties = msgProperties;
        this.contextHelper = new ContextHelperImpl(endpoint, msgProperties);
    }

    @Override
    public void sendEvent(Event event) {
        String domain = event.getDomain();

        String namespace = contextHelper.getValue(domain + "/namespace");
        String servicename = contextHelper.getValue(domain + "/event/servicename");

        sendEvent(event, namespace, servicename);
    }

    @Override
    public void sendEvent(Event event, String targetNamespace, String targetService) {
        try {
            QName service = new QName(targetNamespace, targetService);
            InOnly inOnly = endpoint.getExchangeFactory().createInOnlyExchange();
            inOnly.setService(service);
            inOnly.setOperation(new QName("event"));

            NormalizedMessage msg = inOnly.createMessage();
            inOnly.setInMessage(msg);
            msgProperties.applyToMessage(msg);

            String xml = Transformer.toXml(event);
            msg.setContent(new StringSource(xml));

            endpoint.sendSync(inOnly);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

    }
}
