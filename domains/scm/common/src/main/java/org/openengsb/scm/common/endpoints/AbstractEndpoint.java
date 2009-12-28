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

package org.openengsb.scm.common.endpoints;

import java.io.IOException;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An abstract ProviderEnpoint that provides some convenient methods to get
 * parameters.
 * 
 */
public abstract class AbstractEndpoint extends ProviderEndpoint {
    private static final CachedXPathAPI XPATH = new CachedXPathAPI();

    private Log log = null;

    /* ProviderEndpoint overrides */

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        getLog().info(getEndpointBehaviour());
        try {
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                // call template method
                processInOnlyRequest(exchange, in);
            } else {
                getLog().warn("Exchange was not ACTIVE. Ignoring it.");
            }
        } catch (Exception exception) {
            getLog().error("Encountered an error while " + getEndpointBehaviour(), exception);
            throw exception;
        }
    }

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        getLog().info(getEndpointBehaviour());
        try {
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                // call template method
                processInOutRequest(exchange, in, out);
            } else {
                getLog().warn("Exchange was not ACTIVE. Ignoring it.");
            }
        } catch (Exception exception) {
            getLog().error("Encountered an error while " + getEndpointBehaviour(), exception);
            throw exception;
        }
    }

    /* end ProviderEndpoint overrides */

    /* template methods and default implementations */

    /**
     * Returns a description for the Endpoint's behavior. This is used for
     * logging purposes. If you implement this method, describe, what the
     * Endpoint is about to do. Something like "Adding files".
     */
    protected abstract String getEndpointBehaviour();

    /**
     * Template method that is called from the implementation for processInOut.
     * The default implementation just calls the super-method of processInOut
     * which in turn throws an Exception telling the caller, that this MEP is
     * not supported.
     * 
     * @param exchange see {@link ProviderEndpoint#processInOut}
     * @param in see {@link ProviderEndpoint#processInOut}
     * @param out see {@link ProviderEndpoint#processInOut}
     * @throws Exception see {@link ProviderEndpoint#processInOut}
     */
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        super.processInOut(exchange, in, out);
    }

    /**
     * Template method that is called from the implementation for processInOnly.
     * The default implementation just calls the super-method of processInOut
     * which in turn throws an Exception telling the caller, that this MEP is
     * not supported.
     * 
     * @param exchange see {@link ProviderEndpoint#processInOut}
     * @param in see {@link ProviderEndpoint#processInOut}
     * @throws Exception see {@link ProviderEndpoint#processInOut}
     */
    protected void processInOnlyRequest(MessageExchange exchange, NormalizedMessage in) throws Exception {
        super.processInOnly(exchange, in);
    }

    /* end template methods and default implementations */

    /* helpers */

    /**
     * Convenience method to extract a single Node from a normalized message,
     * indicated by an x-path.
     * 
     * @param inMessage The normalized message.
     * @param xPath The x-path
     * @return The Extracted Node
     * @throws MessagingException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    protected Node extractSingleNode(NormalizedMessage inMessage, String xPath) throws MessagingException,
            TransformerException, ParserConfigurationException, IOException, SAXException {
        Node rootNode = getRootNode(inMessage);
        if (rootNode == null) {
            return null;
        } else {
            return AbstractEndpoint.XPATH.selectSingleNode(rootNode, xPath);
        }
    }

    /**
     * Convenience method to extract a NodeList from a normalized message,
     * indicated by an x-path.
     * 
     * @param inMessage The normalized message.
     * @param xPath The x-path
     * @return The extracted NodeList
     * @throws MessagingException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    protected NodeList extractNodeList(NormalizedMessage inMessage, String xPath) throws MessagingException,
            TransformerException, ParserConfigurationException, IOException, SAXException {
        Node rootNode = getRootNode(inMessage);
        if (rootNode == null) {
            return null;
        } else {
            return AbstractEndpoint.XPATH.selectNodeList(rootNode, xPath);
        }
    }

    /**
     * Convenience method to extract parameter (attribute) from a normalized
     * message, indicated by an x-path.
     * 
     * @param inMessage The normalized message.
     * @param xPath The x-path.
     * @return The extracted value.
     * @throws MessagingException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    protected String extractStringParameter(NormalizedMessage inMessage, String xpath) throws MessagingException,
            TransformerException, ParserConfigurationException, IOException, SAXException {
        // get parameter
        Node node = extractSingleNode(inMessage, xpath);

        // validate them
        if (node == null) {
            return null;
        } else {
            return node.getNodeValue();
        }
    }

    /**
     * Extracts to root-Node (actually XML-Element) from a NormalizedMessage.
     * This helper is needed, since, depending on the way the request was sent
     * (via the domain, or directly) Either a Document-Node or Element-Node is
     * the message's root. To be able to apply the same XPaths either way, this
     * helper "normalizes" the root-Node.
     * 
     * @param message
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    private Node getRootNode(NormalizedMessage message) throws ParserConfigurationException, IOException, SAXException,
            TransformerException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        DOMSource messageXml = sourceTransformer.toDOMSource(message.getContent());

        Node rootNode = messageXml.getNode();

        if (rootNode instanceof Document) {
            return rootNode.getFirstChild();
        } else {
            return rootNode;
        }
    }

    /**
     * This method may seem not very useful, since logger is protected already
     * and could be accessed directly. It exists to change loggers easily. I.e.
     * to exchange the jbi-default-logger with a self-instantiated one.
     * 
     * @return
     */
    protected Log getLog() {
        if (this.logger != null) {
            return this.logger;
        }

        if (this.log == null) {
            this.log = LogFactory.getLog(this.getClass());
        }

        return this.log;
    }

    /* end helpers */
}
