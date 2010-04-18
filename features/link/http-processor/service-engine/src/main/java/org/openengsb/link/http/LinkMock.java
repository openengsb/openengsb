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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

// TODO (re)move latere
/**
 * @org.apache.xbean.XBean element="link-mock"
 */
public class LinkMock extends OpenEngSBEndpoint {

    private static final Log log = LogFactory.getLog(LinkMock.class);

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        log.info("mock called returning default link");
//        Fault artificialFault = exchange.createFault();
//        artificialFault.setContent(new StringSource("<adsf/>"));
//        exchange.setFault(artificialFault);
        out.setContent(new StringSource("<bla/>"));
    }

    @Override
    public void process(MessageExchange exchange) throws Exception {
        // TODO Auto-generated method stub
        super.process(exchange);
    }
}
