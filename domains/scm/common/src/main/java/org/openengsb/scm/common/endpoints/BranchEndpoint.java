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

import org.openengsb.scm.common.ParameterNames;
import org.openengsb.scm.common.commands.Command;


/**
 * The Endpoint to the branch-feature
 */
public class BranchEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "creating new branch.";

    @Override
    protected void processInOnlyRequest(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // get parameters
        String branchName = extractStringParameter(in, "./@" + ParameterNames.BRANCH_NAME);
        String commitMessage = extractStringParameter(in, "./@" + ParameterNames.MESSAGE);

        // log parameters
        if (getLog().isDebugEnabled()) {
            StringBuilder parameters = new StringBuilder();
            parameters.append("Parameters:\n");
            parameters.append("branchName=");
            parameters.append(branchName);
            parameters.append("commitMessage=");
            parameters.append(commitMessage);

            getLog().debug(parameters.toString());
        }

        // validate them
        if ((branchName == null)) {
            throw new IllegalArgumentException("Missing " + ParameterNames.BRANCH_NAME);
        }
        if ((commitMessage == null)) {
            throw new IllegalArgumentException("Missing " + ParameterNames.MESSAGE);
        }

        // execute call
        Command<?> command = getCommandFactory().getBranchCommand(branchName, commitMessage);
        command.execute();
    }

    @Override
    protected String getEndpointBehaviour() {
        return BranchEndpoint.BEHAVIOR;
    }
}
