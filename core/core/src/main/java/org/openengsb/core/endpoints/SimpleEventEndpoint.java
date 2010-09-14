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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public abstract class SimpleEventEndpoint extends EventEndpoint {

    private SourceTransformer t = new SourceTransformer();

    public SimpleEventEndpoint() {
    }

    public SimpleEventEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public SimpleEventEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    protected abstract void handleEvent(Event e, ContextHelper contextHelper, MessageProperties msgProperties);

    @Override
    protected void handleEvent(MessageExchange exchange, NormalizedMessage in, ContextHelper contextHelper,
            MessageProperties msgProperties) throws MessagingException {
        Event e = parseEvent(in);
        handleEvent(e, contextHelper, msgProperties);
    }

    /**
     * parse an Event-message.
     * 
     * @param msg message that should be parsed.
     * @return the parsed Event-Object.
     */
    protected Event parseEvent(NormalizedMessage msg) {
        try {
            String xml = t.toString(msg.getContent());
            return Transformer.toEvent(xml);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
