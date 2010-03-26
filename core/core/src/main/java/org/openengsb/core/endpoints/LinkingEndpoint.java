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
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;

public abstract class LinkingEndpoint<T> extends RPCEndpoint<T> {
    public LinkingEndpoint() {
        super();
    }

    public LinkingEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public LinkingEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    @Override
    protected void inOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper, MessageProperties msgProperties) throws Exception {
        // not required in this use case
    }

    @Override
    protected QName getForwardTarget(ContextHelper contextHelper) {
        // not required in this use case
        return null;
    }

}
