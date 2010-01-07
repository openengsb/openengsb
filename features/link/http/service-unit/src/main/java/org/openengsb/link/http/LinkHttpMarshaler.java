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

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.http.endpoints.AbstractHttpConsumerMarshaler;
import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * serves as an interface between the http-service-unit and the associated
 * processor-service. It is responsible for the conversion of HTTP-requests to
 * JBI-messages and vice versa
 */
public class LinkHttpMarshaler extends AbstractHttpConsumerMarshaler {

    private static final String HTML_FAULT_BODY = "JBI-fault occured: %s";
    private static final String HTML_RESPONSE_BODY = "<html><body><h1>You are %s</h1> %s</body></html>";
    private static final String WHOAMI_REQUEST = "<whoami>%s</whoami>";
    private static final String LINK_REQUEST_QUERY = "<httpLinkRequest><query>%s</query><requestorIP>%s</requestorIP></httpLinkRequest>";
    public static final String WHOAMI = "whoami";
    private static final Log log = LogFactory.getLog(LinkHttpMarshaler.class);

    private InOnly createNewInOnlyExchange(ComponentContext context) throws MessagingException {
        DeliveryChannel channel = context.getDeliveryChannel();
        MessageExchangeFactory factory = channel.createExchangeFactory();
        InOnly result = factory.createInOnlyExchange();
        return result;
    }

    private String prependMissingUUID(String query) {
        if (!query.startsWith("UUID:")) {
            query = "UUID:" + query;
        }
        return query;
    }

    private String createLinkQueryMessageFromRequest(HttpServletRequest request) {
        String query = request.getQueryString();
        String ip = request.getRemoteAddr();
        if (query.equalsIgnoreCase(WHOAMI)) {
            return String.format(WHOAMI_REQUEST, ip);
        } else {
            query = prependMissingUUID(query);
            return String.format(LINK_REQUEST_QUERY, query, ip);
        }
    }

    @Override
    public MessageExchange createExchange(HttpServletRequest request, ComponentContext context) throws Exception {
        InOnly result = createNewInOnlyExchange(context);
        NormalizedMessage inMessage = result.createMessage();

        String message = createLinkQueryMessageFromRequest(request);
        Source content = new StringSource(message);
        inMessage.setContent(content);
        result.setInMessage(inMessage);

        log.info("create exchange with content: " + content);

        return result;
    }

    @Override
    public void sendAccepted(MessageExchange exchange, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        /* always send a HTML-document containing the clients remote IP-address */
        // TODO maybe make more beautiful response
        response.getWriter()
                .append(String.format(HTML_RESPONSE_BODY, request.getRemoteAddr(), request.getRemoteHost()));
        log.debug("send HTTP-accepted");
    }

    @Override
    public void sendError(MessageExchange exchange, Exception error, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        error.printStackTrace(response.getWriter());
        log.error(error.getMessage());
        error.printStackTrace();
    }

    @Override
    public void sendFault(MessageExchange exchange, Fault fault, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        response.getWriter().append(String.format(HTML_FAULT_BODY, fault.getContent().toString()));
        log.error("send JBI-fault");
        log.error(fault.getContent());

    }

    @Override
    public void sendOut(MessageExchange exchange, NormalizedMessage outMsg, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        log.info("send out");
    }

}
