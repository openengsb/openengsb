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
package org.openengsb.link.http;

import java.io.IOException;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
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
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.link.http.LinkHttpMarshaler;
import org.openengsb.util.tuple.Triple;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @org.apache.xbean.XBean element="link-processor"
 */
public class LinkHttpProcessEndpoint extends ProviderEndpoint {

    private static Log log = LogFactory.getLog(LinkHttpProcessEndpoint.class);

    private static final String linkRequestMessageString = "  <LinkQueryRequestMessage>" + "<body>"
            + " <query>%s</query>" + "</body>" + "</LinkQueryRequestMessage>";

    private static final SourceTransformer st = new SourceTransformer();

    private enum RequestType {
        WHOAMI, LINK_REQUEST,
    }

    protected QName linkServiceName;
    protected QName jmsServiceName;

    @Override
    public void validate() throws DeploymentException {
        if (linkServiceName == null || jmsServiceName == null) {
            throw new DeploymentException("linkServiceName and jmsServiceName must be set");
        }
        super.validate();
    }

    /**
     * @return the linkServiceName
     */
    public final QName getLinkServiceName() {
        return linkServiceName;
    }

    /**
     * @param linkServiceName the linkServiceName to set
     */
    public final void setLinkServiceName(QName linkServiceName) {
        this.linkServiceName = linkServiceName;
    }

    /**
     * @return the jmsServiceName
     */
    public final QName getJmsServiceName() {
        return jmsServiceName;
    }

    /**
     * @param jmsServiceName the jmsServiceName to set
     */
    public final void setJmsServiceName(QName jmsServiceName) {
        this.jmsServiceName = jmsServiceName;
    }

    public LinkHttpProcessEndpoint() {
        super();
        // TODO Auto-generated constructor stub
    }

    public LinkHttpProcessEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
        // TODO Auto-generated constructor stub
    }

    public LinkHttpProcessEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
        // TODO Auto-generated constructor stub
    }

    /**
     * creates a request-message for the link-service
     *
     * @param linkId
     * @return
     */
    private String createLinkRequestMessage(String linkId) {
        return String.format(linkRequestMessageString, linkId);
    }

    /*
     * Triple-structure: a: integer inidicating the message type; b: linkId or
     * "whoami"; c: ip
     */
    /**
     * parses a query-message sent by the link-http-service
     *
     * @param source content of the message
     * @return Triple containing the parsed result (messagetype, linkid, ip)
     * @throws ParserConfigurationException if source-transformation fails
     * @throws IOException if source-transformation fails
     * @throws SAXException if source-transformation fails
     * @throws TransformerException if source-transformation fails
     */
    private Triple<RequestType, String, String> parseQueryMessage(Source source) throws ParserConfigurationException,
            IOException, SAXException, TransformerException {
        DOMSource dSource = st.toDOMSource(source);
        Node rootNode = dSource.getNode().getFirstChild();

        if (rootNode.getNodeName().equals("whoami")) {
            String ip = rootNode.getFirstChild().getTextContent();
            return new Triple<RequestType, String, String>(RequestType.WHOAMI, LinkHttpMarshaler.STRING_WHOAMI, ip);
        } else {
            NodeList children = rootNode.getChildNodes();
            if (children.getLength() == 2) {
                String linkId = children.item(0).getTextContent();
                String ip = children.item(1).getTextContent();
                return new Triple<RequestType, String, String>(RequestType.LINK_REQUEST, linkId, ip);
            } else {
                throw new IllegalArgumentException("invalid request-xml, parameter is missing. " + dSource.toString());
            }
        }

    }

    private void processMessage(MessageExchange exchange, NormalizedMessage in) throws Exception {

        DOMSource source = st.toDOMSource(in.getContent());
        Triple<RequestType, String, String> parsedQuery = parseQueryMessage(source);

        if (parsedQuery.fst == RequestType.WHOAMI) {
            return;
        }

        /* request the link from the corresponding service */
        InOut linkEx = this.getExchangeFactory().createInOutExchange();
        linkEx.setService(linkServiceName);
        NormalizedMessage linkExIn = linkEx.createMessage();
        linkExIn.setContent(new StringSource(createLinkRequestMessage(parsedQuery.trd)));
        linkEx.setInMessage(linkExIn);
        getChannel().sendSync(linkEx);

        DOMSource linkResponse = st.toDOMSource(linkEx.getOutMessage().getContent());

        /* forward the result to the jms-service */
        log.info("link-service returned message: " + st.toString(linkResponse));
        InOut jmsEx = this.getExchangeFactory().createInOutExchange();
        jmsEx.setService(jmsServiceName);
        NormalizedMessage jmsMsg = jmsEx.createMessage();
        jmsMsg.setContent(linkResponse);

        /* the ip-property is used as message-filter on each client. */
        jmsMsg.setProperty("ip", parsedQuery.snd);
        jmsEx.setInMessage(jmsMsg);
        getChannel().send(jmsEx);

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

}
