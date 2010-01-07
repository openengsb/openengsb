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
package org.openengsb.scm.common.endpoints;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.util.StringArraySerializer;


/**
 * The Endpoint to the list-branches-feature
 */
public class ListBranchesEndpoint extends AbstractScmEndpoint {
    private static final String RESULT_NAME = "branches";
    private static final String BEHAVIOR = "listing branches.";

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        Command<String[]> command = getCommandFactory().getListBranchesCommand();
        String[] result = command.execute();

        // xml-ify result and send it back
        out.setContent(StringArraySerializer.serialize(result, ListBranchesEndpoint.RESULT_NAME));
        // getChannel ().send (exchange);
    }

    @Override
    protected String getEndpointBehaviour() {
        return ListBranchesEndpoint.BEHAVIOR;
    }

}
