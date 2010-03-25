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
package org.openengsb.context;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.LinkingEndpoint;

/**
 * @org.apache.xbean.XBean element="contextEndpoint"
 *                         description="Context Component"
 */
public class ContextEndpoint extends LinkingEndpoint<ContextHelper> {

    private EndpointContextHelperImpl contextImpl = new EndpointContextHelperImpl();

    @Override
    protected ContextHelper getImplementation(ContextHelper contextHelper, MessageProperties msgProperties) {
        contextImpl.setCurrentId(msgProperties.getContextId());
        return contextImpl;
    }

}
