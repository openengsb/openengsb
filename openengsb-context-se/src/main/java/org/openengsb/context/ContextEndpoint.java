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

import java.io.IOException;
import java.util.Map.Entry;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @org.apache.xbean.XBean element="contextEndpoint"
 *                         description="Context Component"
 */
public class ContextEndpoint extends ProviderEndpoint {
    private static final String ID_XPATH = "/message/header/contextID";

    private ContextStore contextStore = new ContextStore();

    private static final CachedXPathAPI XPATH = new CachedXPathAPI();

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        Node idNode = extractSingleNode(in, ID_XPATH);

        if (idNode == null) {
            throw new IllegalStateException("Could not find id node");
        }

        String id = idNode.getTextContent();
        Context ctx = contextStore.getContext(id);

        String m = "<context>\n";
        for (Entry<String, String> e : ctx.flatten().entrySet()) {
            m += String.format("<entry key=\"%s\" value=\"%s\" />\n", e.getKey(), e.getValue());
        }
        m += "</context>";

        NormalizedMessageImpl msg = new NormalizedMessageImpl();
        msg.setContent(new StringSource(m));

        if (exchange != null) {
            InOut inOut = getExchangeFactory().createInOutExchange();
            inOut.setInMessage(msg);
        }
        out.setContent(msg.getContent());
    }

    private Node extractSingleNode(NormalizedMessage inMessage, String xPath) throws MessagingException,
            TransformerException, ParserConfigurationException, IOException, SAXException {
        Node rootNode = getRootNode(inMessage);
        return rootNode == null ? null : XPATH.selectSingleNode(rootNode, xPath);
    }

    private Node getRootNode(NormalizedMessage message) throws ParserConfigurationException, IOException, SAXException,
            TransformerException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        DOMSource messageXml = sourceTransformer.toDOMSource(message.getContent());

        Node rootNode = messageXml.getNode();

        if (rootNode instanceof Document) {
            return rootNode.getFirstChild();
        }

        return rootNode;
    }
}
