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
package org.openengsb.link.http;

import java.io.IOException;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.api.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @org.apache.xbean.XBean element="link-processor"
 */
public class LinkHttpProcessEndpoint extends OpenEngSBEndpoint {
    private static Log log = LogFactory.getLog(LinkHttpProcessEndpoint.class);

    private static final String LINK_REQUEST_MESSAGE = "  <LinkQueryRequestMessage>" + "<body>" + " <query>%s</query>"
            + "</body>" + "</LinkQueryRequestMessage>";

    private static final SourceTransformer st = new SourceTransformer();

    private enum RequestType {
        WHOAMI, LINK_REQUEST,
    }

    protected QName linkServiceName;
    protected QName jmsServiceName;

    private ServiceMixClient client;

    private static class RequestMessage {
        public String ip;
        public RequestType requestType;
        public String linkId;

        public RequestMessage(String ip, RequestType requestType, String linkId) {
            this.ip = ip;
            this.requestType = requestType;
            this.linkId = linkId;
        }
    }

    public LinkHttpProcessEndpoint() {
        super();
    }

    public LinkHttpProcessEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public LinkHttpProcessEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    @Override
    public void validate() throws DeploymentException {
        if (linkServiceName == null || jmsServiceName == null) {
            throw new DeploymentException("linkServiceName and jmsServiceName must be set");
        }
        super.validate();
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
    }

    private void processMessage(MessageExchange exchange, NormalizedMessage in) throws Exception {
        DOMSource source = st.toDOMSource(in.getContent());
        RequestMessage parsedQuery = parseQueryMessage(source);

        if (parsedQuery.requestType == RequestType.WHOAMI) {
            return;
        }

        InOut linkExchange = requestLinkFromService(parsedQuery.linkId);
        if (!checkForFaultAndForward(exchange, linkExchange)) {
            return;
        }

        NormalizedMessage linkExchangeOut = linkExchange.getOutMessage();
        DOMSource linkResponse = st.toDOMSource(linkExchangeOut.getContent());

        /* DOMSource linkResponse = requestLinkFromService(parsedQuery.linkId); */
        log.info("link-service returned message: " + st.toString(linkResponse));

        InOut jmsExchange = sendJmsResponse(parsedQuery.ip, linkResponse);
        checkForFaultAndForward(exchange, jmsExchange);
    }

    private boolean checkForFaultAndForward(MessageExchange exchange, MessageExchange subExchange)
            throws MessagingException {
        Fault fault = subExchange.getFault();
        if (fault != null) {
            log.error(exchange.getPattern() + " errored");
            exchange.setFault(fault);
            return false;
        }
        log.info(exchange.getPattern() + " worked normally");
        return true;
    }

    private RequestMessage parseQueryMessage(Source source) throws ParserConfigurationException, IOException,
            SAXException, TransformerException {
        DOMSource dSource = st.toDOMSource(source);
        Node rootNode = dSource.getNode().getFirstChild();

        if (rootNode.getNodeName().equals(LinkHttpMarshaler.WHOAMI)) {
            String ip = rootNode.getFirstChild().getTextContent();
            return new RequestMessage(ip, RequestType.WHOAMI, LinkHttpMarshaler.WHOAMI);
        } else {
            return parseHttpLinkRequest(dSource, rootNode);
        }
    }

    private RequestMessage parseHttpLinkRequest(DOMSource dSource, Node rootNode) {
        NodeList children = rootNode.getChildNodes();
        if (children.getLength() == 2) {
            String linkId = children.item(0).getTextContent();
            String ip = children.item(1).getTextContent();
            return new RequestMessage(ip, RequestType.LINK_REQUEST, linkId);
        }
        throw new IllegalArgumentException("invalid request-xml, parameter is missing. " + dSource.toString());
    }

    private InOut sendJmsResponse(String ip, DOMSource linkResponse) throws MessagingException {
        InOut jmsEx = client.createInOutExchange();
        jmsEx.setService(jmsServiceName);

        NormalizedMessage jmsMsg = jmsEx.getInMessage();
        jmsMsg.setContent(linkResponse);
        /* the ip-property is used as message-filter on each client. */
        jmsMsg.setProperty("ip", ip);

        log.info("sending jms-response");
        client.sendSync(jmsEx);
        return jmsEx;
    }

    private InOut requestLinkFromService(String linkId) throws MessagingException, ParserConfigurationException,
            IOException, SAXException, TransformerException {
        InOut linkExchange = getExchangeFactory().createInOutExchange();
        linkExchange.setService(linkServiceName);

        NormalizedMessage linkExIn = linkExchange.getInMessage();
        linkExIn.setContent(createLinkRequestMessage(linkId));
        sendSync(linkExchange);

        return linkExchange;

    }

    private Source createLinkRequestMessage(String linkId) {
        String message = String.format(LINK_REQUEST_MESSAGE, linkId);
        return new StringSource(message);
    }

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        processMessage(exchange, in);
    }

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        processMessage(exchange, in);
        // TODO return anything more or different?
        out.setContent(new StringSource("<success/>"));
    }

    public final QName getLinkServiceName() {
        return linkServiceName;
    }

    public final void setLinkServiceName(QName linkServiceName) {
        this.linkServiceName = linkServiceName;
    }

    public final QName getJmsServiceName() {
        return jmsServiceName;
    }

    public final void setJmsServiceName(QName jmsServiceName) {
        this.jmsServiceName = jmsServiceName;
    }

}
