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

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.MessageProperties;

public abstract class EventEndpoint extends OpenEngSBEndpoint {

    public EventEndpoint() {
    }

    public EventEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public EventEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        MessageProperties msgProperties = readProperties(in);
        ContextHelper contextHelper = new ContextHelperImpl(this, msgProperties);

        QName operation = exchange.getOperation();
        if (operation == null || !operation.getLocalPart().equals("event")) {
            throw new IllegalStateException("Operation should be event but is " + operation);
        }

        handleEvent(exchange, in, contextHelper, msgProperties);

    }

    protected abstract void handleEvent(MessageExchange exchange, NormalizedMessage in, ContextHelper contextHelper,
            MessageProperties msgProperties) throws MessagingException;

}
