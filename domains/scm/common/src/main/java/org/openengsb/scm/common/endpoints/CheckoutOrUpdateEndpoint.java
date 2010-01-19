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

import org.openengsb.drools.model.MergeResult;
import org.openengsb.scm.common.ParameterNames;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.util.MergeResultSerializer;

/**
 * The Endpoint to the checkout-feature
 */
public class CheckoutOrUpdateEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "checking out or updating.";

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        // get parameters
        String author = extractStringParameter(in, "./@" + ParameterNames.AUTHOR);

        // log parameters
        if (getLog().isDebugEnabled()) {
            StringBuilder parameters = new StringBuilder();
            parameters.append("Parameters:\n");
            parameters.append("author=");
            parameters.append(author);

            getLog().debug(parameters.toString());
        }

        // validate them
        if (author == null) {
            author = "dummyAuthor";
            // throw new IllegalArgumentException ("Missing " +
            // ParameterNames.AUTHOR);
        }

        // execute call
        Command<MergeResult> command = getCommandFactory().getCheckoutOrUpdateCommand(author);
        MergeResult result = command.execute();

        // xml-ify result and send it back
        out.setContent(MergeResultSerializer.serialize(result));
        // getChannel ().send (exchange);
    }

    @Override
    protected String getEndpointBehaviour() {
        return CheckoutOrUpdateEndpoint.BEHAVIOR;
    }
}
