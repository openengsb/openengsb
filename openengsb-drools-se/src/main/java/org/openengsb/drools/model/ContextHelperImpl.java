/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.openengsb.drools.model;

import java.util.UUID;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.drools.DroolsEndpoint;
import org.openengsb.util.serialization.SerializationException;

public class ContextHelperImpl {

    private final DroolsEndpoint endpoint;
    private final String contextId;

    public ContextHelperImpl(DroolsEndpoint endpoint, String contextId) {
        this.endpoint = endpoint;
        this.contextId = contextId;
    }

    public String getValue(String key) {
        try {
            InOut inout = new InOutImpl(UUID.randomUUID().toString());
            inout.setService(new QName("urn:openengsb:context", "contextService"));
            // inout.setInterfaceName(new QName("contextEndpoint"));

            NormalizedMessage msg = inout.createMessage();
            inout.setInMessage(msg);
            msg.setProperty("messageType", "context/request");
            TextSegment text = new TextSegment.Builder("path").text(contextId + "/" + key).build();
            String xml = text.toXML();

            msg.setContent(new StringSource(xml));

            endpoint.sendSync(inout);

            NormalizedMessage response = inout.getOutMessage();

            System.out.println(response.getContent());
            return "Value: " + key;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
