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
package org.openengsb.context;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.WorkingDirectory;
import org.openengsb.util.serialization.SerializationException;

/**
 * @org.apache.xbean.XBean element="contextEndpoint"
 *                         description="Context Component"
 */
public class ContextEndpoint extends ProviderEndpoint {

    private ContextStore contextStore = new ContextStore(WorkingDirectory.getFile("context", "contextdata.xml"));

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        if (!(exchange instanceof RobustInOnly)) {
            throw new UnsupportedOperationException("Unsupported MEP: " + exchange.getPattern());
        }
        RobustInOnly ex = (RobustInOnly) exchange;

        String messageType = getMessageType(in);
        String id = getContextId(in);

        if (messageType.equals("context/store")) {
            handleStore(id, in);
        } else {
            // TODO throw specialized exception
            throw new RuntimeException("Illegal message type: " + messageType);
        }
    }

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        String id = getContextId(in);
        String messageType = getMessageType(in);

        String result = null;
        if (messageType == null) {
            throw new RuntimeException("MessageType not set");
        } else if (messageType.equals("context/request")) {
            result = handleRequest(in, id);
        } else if (messageType.equals("context/store")) {
            handleStore(id, in);
            result = "<result>success</result>";
        } else {
            throw new RuntimeException("Illegal message type: " + messageType);
        }

        out.setContent(new StringSource(result));
    }

    private void handleStore(String id, NormalizedMessage in) throws SerializationException, TransformerException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        String inputMessage = sourceTransformer.toString(in.getContent());

        Segment inSegment = Segment.fromXML(inputMessage);
        ListSegment ls = (ListSegment) inSegment;

        for (Segment s : ls.getList()) {
            handleStoreSegment((TextSegment) s, id);
        }
    }

    private void handleStoreSegment(TextSegment s, String id) {
        String key = s.getName();
        String value = s.getText();
        contextStore.setValue(id + "/" + key, value);
    }

    private String handleRequest(NormalizedMessage in, String id) throws TransformerException, SerializationException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        String inputMessage = sourceTransformer.toString(in.getContent());

        Segment inSegment = Segment.fromXML(inputMessage);
        TextSegment ts = (TextSegment) inSegment;
        String path = ts.getText();

        Context ctx = contextStore.getContext(id + "/" + path);

        Segment segment = ContextSegmentTransformer.toSegment(ctx);
        return segment.toXML();
    }

    private String getMessageType(NormalizedMessage in) {
        return (String) in.getProperty("messageType");
    }

    private String getContextId(NormalizedMessage in) {
        return (String) in.getProperty("contextId");
    }
}
