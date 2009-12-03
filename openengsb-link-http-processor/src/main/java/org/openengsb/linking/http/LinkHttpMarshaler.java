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

public class LinkHttpMarshaler extends AbstractHttpConsumerMarshaler {

    private static final Log log = LogFactory.getLog(LinkHttpMarshaler.class);

    @Override
    public MessageExchange createExchange(HttpServletRequest request, ComponentContext context) throws Exception {
        InOnly result = context.getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
        NormalizedMessage inMessage = result.createMessage();
        String query = request.getQueryString();
        String ip = request.getRemoteAddr();
        String msg = "<httpLinkRequest><query>%s</query><requestorIP>%s</requestorIP></httpLinkRequest>";
        Source content = new StringSource(String.format(msg, query, ip));
        inMessage.setContent(content);
        result.setInMessage(inMessage);
        return result;
    }

    @Override
    public void sendAccepted(MessageExchange exchange, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // TODO Auto-generated method stub
        response.getWriter().append("<html><body><h1>Request accepted</h1></body></html>");
        log.info("sendaccept");
    }

    @Override
    public void sendError(MessageExchange exchange, Exception error, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // TODO Auto-generated method stub
        error.printStackTrace();
        log.info("send error");
    }

    @Override
    public void sendFault(MessageExchange exchange, Fault fault, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        log.info("senf fault");
        // TODO Auto-generated method stub

    }

    public void sendOut(MessageExchange exchange, javax.jbi.messaging.NormalizedMessage outMsg,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("send out");
    };

}
