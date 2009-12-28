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

package org.openengsb.contextcommon;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.openengsb.core.OpenEngSBEndpoint;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class ContextHelperImpl implements ContextHelper {

    private final OpenEngSBEndpoint<?> endpoint;
    private final String contextId;

    public ContextHelperImpl(OpenEngSBEndpoint<?> endpoint, String contextId) {
        this.endpoint = endpoint;
        this.contextId = contextId;
    }

    public String getValue(String pathAndKey) {
        try {
            InOut inout = new InOutImpl(UUID.randomUUID().toString());
            inout.setService(new QName("urn:openengsb:context", "contextService"));

            NormalizedMessage msg = inout.createMessage();
            inout.setInMessage(msg);
            msg.setProperty("messageType", "context/request");
            msg.setProperty("contextId", contextId);

            String path = pathAndKey.substring(0, pathAndKey.lastIndexOf('/'));
            String key = pathAndKey.substring(pathAndKey.lastIndexOf('/') + 1);

            TextSegment text = new TextSegment.Builder("path").text(path).build();
            String xml = text.toXML();

            msg.setContent(new StringSource(xml));

            endpoint.sendSync(inout);

            NormalizedMessage response = inout.getOutMessage();
            String outXml = new SourceTransformer().toString(response.getContent());
            Segment segment = Segment.fromXML(outXml);

            Context context = ContextSegmentTransformer.toContext(segment);
            String value = context.get(key);

            return value;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getAllValues(String path) {
        try {
            InOut inout = new InOutImpl(UUID.randomUUID().toString());
            inout.setService(new QName("urn:openengsb:context", "contextService"));

            NormalizedMessage msg = inout.createMessage();
            inout.setInMessage(msg);
            msg.setProperty("messageType", "context/request");
            msg.setProperty("contextId", contextId);

            TextSegment text = new TextSegment.Builder("path").text(path).build();
            String xml = text.toXML();

            msg.setContent(new StringSource(xml));

            endpoint.sendSync(inout);

            NormalizedMessage response = inout.getOutMessage();
            String outXml = new SourceTransformer().toString(response.getContent());
            Segment segment = Segment.fromXML(outXml);

            Context context = ContextSegmentTransformer.toContext(segment);
            Set<String> keys = context.getKeys();
            Map<String, String> values = new HashMap<String, String>();
            for (String key : keys) {
                values.put(key, context.get(key));
            }
            return values;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
