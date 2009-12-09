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

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
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

    public static final String STRING_WHOAMI = "whoami";
    private static final Log log = LogFactory.getLog(LinkHttpMarshaler.class);

    @Override
    public MessageExchange createExchange(HttpServletRequest request, ComponentContext context) throws Exception {
        InOnly result = context.getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
        NormalizedMessage inMessage = result.createMessage();
        String query = request.getQueryString();
        String ip = request.getRemoteAddr();

        /* parse the query */
        if (query.startsWith("UUID:")) {
            query = query.substring(5);
        } else if (!query.equalsIgnoreCase(STRING_WHOAMI)) {
            return result;
        }

        /* create a message with the parsed query as content */
        String msg = "<httpLinkRequest><query>%s</query><requestorIP>%s</requestorIP></httpLinkRequest>";
        Source content = new StringSource(String.format(msg, query, ip));
        log.info("create exchange with content: " + content);
        inMessage.setContent(content);
        result.setInMessage(inMessage);
        return result;
    }

    @Override
    public void sendAccepted(MessageExchange exchange, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        /* always send a HTML-document containing the clients remote IP-address */
        response.getWriter().append(
                "<html><body><h1>You are " + request.getRemoteAddr() + "</h1>" + request.getRemoteHost()
                        + "</body></html>");
        // TODO more beautiful response
        log.debug("send HTTP-accepted");
    }

    @Override
    public void sendError(MessageExchange exchange, Exception error, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // TODO expose the error to the user completely?
        error.printStackTrace(response.getWriter());
        log.error(error.getMessage());
        error.printStackTrace();
    }

    @Override
    public void sendFault(MessageExchange exchange, Fault fault, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        response.getWriter().append("JBI-fault occured: " + fault.getContent().toString());
        log.error("send JBI-fault");
        log.error(fault.getContent());

    }

    @Override
    public void sendOut(MessageExchange exchange, NormalizedMessage outMsg, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        log.info("send out");
    };

}
