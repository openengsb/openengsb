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
package org.openengsb.notification;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * @org.apache.xbean.XBean element="notificationEndpoint"
 *                         description="Notification Endpoint"
 */
public class NotificationEndpoint extends ProviderEndpoint {

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        String id = getContextId(in);
        String messageType = getMessageType(in);

        String result = null;
        if (messageType == null) {
            throw new RuntimeException("MessageType not set");
        } else if (messageType.equals("context/request")) {
            // result = handleRequest(in, id);
        } else if (messageType.equals("context/store")) {
            // handleStore(id, in);
            result = "<result>success</result>";
        } else {
            throw new RuntimeException("Illegal message type: " + messageType);
        }

        out.setContent(new StringSource(result));
    }

    private String getMessageType(NormalizedMessage in) {
        return (String) in.getProperty("messageType");
    }

    private String getContextId(NormalizedMessage in) {
        return (String) in.getProperty("contextId");
    }
}
