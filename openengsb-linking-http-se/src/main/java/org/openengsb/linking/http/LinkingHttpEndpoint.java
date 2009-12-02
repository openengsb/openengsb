package org.openengsb.linking.http;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.apache.camel.converter.jaxp.StringSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @org.apache.xbean.XBean element="link-processor"
 */
public class LinkingHttpEndpoint extends ProviderEndpoint {

    private static Log log = LogFactory.getLog(LinkingHttpEndpoint.class);
    private static final SourceTransformer st = new SourceTransformer();

    public LinkingHttpEndpoint() {
        super();
        // TODO Auto-generated constructor stub
    }

    public LinkingHttpEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
        // TODO Auto-generated constructor stub
    }

    public LinkingHttpEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
        // TODO Auto-generated constructor stub
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
            ip = children.item(0).getTextContent();
        }
        /* done parsing */

        // TODO forward request to link-su

        // TODO send response via jms

        log.info("requesting Link: " + linkId + " for ip: " + ip);
    }

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        processMessage(exchange, in);
    }

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        processMessage(exchange, in);
        out.setContent(new StringSource("<success/>"));
    }

    @Override
    public synchronized void start() throws Exception {
        // new Thread(new ListenerThread(serverPort)).start();
        super.start();
    }

}
