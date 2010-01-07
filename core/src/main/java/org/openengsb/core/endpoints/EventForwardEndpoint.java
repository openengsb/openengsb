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
package org.openengsb.core.endpoints;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.openengsb.contextcommon.ContextHelper;

public class EventForwardEndpoint extends EventEndpoint {

    @Override
    protected void handleEvent(MessageExchange exchange, NormalizedMessage in, ContextHelper contextHelper)
            throws MessagingException {
        String namespace = contextHelper.getValue("event/defaultTarget/namespace");
        String servicename = contextHelper.getValue("event/defaultTarget/servicename");
        QName service = new QName(namespace, servicename);
        forwardInOnlyMessage(exchange, in, service);
    }
}
