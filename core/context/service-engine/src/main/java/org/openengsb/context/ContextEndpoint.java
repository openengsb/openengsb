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
package org.openengsb.context;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextSegmentTransformer;
import org.openengsb.contextcommon.ContextStore;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.WorkingDirectory;
import org.openengsb.util.serialization.SerializationException;

/**
 * @org.apache.xbean.XBean element="contextEndpoint"
 *                         description="Context Component"
 */
public class ContextEndpoint extends OpenEngSBEndpoint {

    private ContextStore contextStore = new ContextStore(WorkingDirectory.getFile("context", "contextdata.xml"));

    private void handleAddContext(String id, NormalizedMessage in) throws SerializationException, TransformerException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        String inputMessage = sourceTransformer.toString(in.getContent());

        Segment inSegment = Segment.fromXML(inputMessage);
        ListSegment ls = (ListSegment) inSegment;

        for (Segment s : ls.getList()) {
            handleAddContextSegment((TextSegment) s, id);
        }
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

    private void handleRemove(String id, NormalizedMessage in) throws SerializationException, TransformerException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        String inputMessage = sourceTransformer.toString(in.getContent());

        Segment inSegment = Segment.fromXML(inputMessage);
        ListSegment ls = (ListSegment) inSegment;

        for (Segment s : ls.getList()) {
            handleRemoveSegment((TextSegment) s, id);
        }
    }

    private void handleStoreSegment(TextSegment s, String id) {
        String key = s.getName();
        String value = s.getText();
        contextStore.setValue(id + "/" + key, value);
    }

    private void handleAddContextSegment(TextSegment s, String id) {
        String path = s.getName();
        contextStore.addContext(id + "/" + path);
    }

    private void handleRemoveSegment(TextSegment s, String id) {
        String key = s.getName();
        contextStore.removeValue(id + "/" + key);
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

    @Override
    protected final void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }
        MessageProperties msgProperties = readProperties(in);
        String id = msgProperties.getContextId();
        String operation = exchange.getOperation().getLocalPart();

        String result = null;
        if (operation == null) {
            throw new RuntimeException("Operation not set");
        } else if (operation.equals("request")) {
            result = handleRequest(in, id);
        } else if (operation.equals("store")) {
            handleStore(id, in);
            result = "<result>success</result>";
        } else if (operation.equals("addContext")) {
            handleAddContext(id, in);
            result = "<result>success</result>";
        } else if (operation.equals("remove")) {
            handleRemove(id, in);
            result = "<result>success</result>";
        } else {
            throw new RuntimeException("Illegal operation: " + operation);
        }

        out.setContent(new StringSource(result));
    }

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }
        MessageProperties msgProperties = readProperties(in);
        String id = msgProperties.getContextId();
        String operation = exchange.getOperation().getLocalPart();

        if (operation == null) {
            throw new RuntimeException("Operation not set");
        } else if (operation.equals("store")) {
            handleStore(id, in);
        } else if (operation.equals("addContext")) {
            handleAddContext(id, in);
        } else if (operation.equals("remove")) {
            handleRemove(id, in);
        } else {
            throw new RuntimeException("Illegal operation: " + operation + " for message exchange pattern: "
                    + exchange.getPattern());
        }
    }

}
