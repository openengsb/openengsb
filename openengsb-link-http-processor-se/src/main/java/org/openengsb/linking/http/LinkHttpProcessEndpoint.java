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
package org.openengsb.linking.http;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @org.apache.xbean.XBean element="link-processor"
 */
public class LinkHttpProcessEndpoint extends ProviderEndpoint {

    private static Log log = LogFactory.getLog(LinkHttpProcessEndpoint.class);
    private static final SourceTransformer st = new SourceTransformer();

    /*
     * TODO configure with spring
     */
    private QName linkServiceName;
    private QName jmsServiceName;

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

    private static final String linkRequestMessageString = "  <LinkQueryRequestMessage>" + "<body>"
            + " <query>%s</query>" + "</body>" + "</LinkQueryRequestMessage>";

    private String createLinkRequestMessage(String linkId) {
        return String.format(linkRequestMessageString, linkId);
    }

    private void processMessage(MessageExchange exchange, NormalizedMessage in) throws Exception {
        String linkId;
        String ip;

        /* parse message */
        DOMSource source = st.toDOMSource(in.getContent());
        Node rootNode = source.getNode().getFirstChild();
        log.info("nodeName: " + rootNode.getNodeName());
        NodeList children = rootNode.getChildNodes();
        log.info(children.getLength());
        if (children.getLength() != 2) {
            throw new IllegalArgumentException("invalid request-xml, parameter is missing");
        } else {
            linkId = children.item(0).getTextContent();
            ip = children.item(1).getTextContent();
        }
        /* done parsing */
        log.info("requesting Link: " + linkId + " for ip: " + ip);

        InOut linkEx = this.getExchangeFactory().createInOutExchange();
        linkEx.setService(linkServiceName);

        NormalizedMessage linkExIn = linkEx.createMessage();
        linkExIn.setContent(new StringSource(createLinkRequestMessage(linkId)));
        linkEx.setInMessage(linkExIn);
        getChannel().sendSync(linkEx);
        DOMSource linkResponse = st.toDOMSource(linkEx.getOutMessage().getContent());

        log.info("link-service returned message: " + st.toString(linkResponse));
        InOut jmsEx = this.getExchangeFactory().createInOutExchange();
        jmsEx.setService(jmsServiceName);
        NormalizedMessage jmsMsg = jmsEx.createMessage();
        jmsMsg.setContent(linkResponse);
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
