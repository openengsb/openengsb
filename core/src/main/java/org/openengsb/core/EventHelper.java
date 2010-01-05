package org.openengsb.core;

import java.util.UUID;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOnlyImpl;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

/**
 * Copyright 2009 OpenEngSB Division, Vienna University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class EventHelper {

    private ContextHelper contextHelper;

    private String contextId;

    private OpenEngSBEndpoint endpoint;

    public EventHelper(OpenEngSBEndpoint endpoint, String contextId) {
        this.endpoint = endpoint;
        this.contextId = contextId;
        this.contextHelper = new ContextHelperImpl(endpoint, contextId);
    }

    public void sendEvent(Event event) {
        try {
            String domain = event.getDomain();

            String namespace = contextHelper.getValue(domain + "/namespace");
            String servicename = contextHelper.getValue(domain + "/event/servicename");

            QName service = new QName(namespace, servicename);
            InOnly inOnly = new InOnlyImpl(UUID.randomUUID().toString());
            inOnly.setService(service);

            NormalizedMessage msg = inOnly.createMessage();
            inOnly.setInMessage(msg);
            msg.setProperty("messageType", "event");
            msg.setProperty("contextId", contextId);

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
