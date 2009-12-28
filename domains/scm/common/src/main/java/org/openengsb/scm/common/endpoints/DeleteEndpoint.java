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

package org.openengsb.scm.common.endpoints;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.openengsb.scm.common.ParameterNames;
import org.openengsb.scm.common.commands.Command;


/**
 * The Endpoint to the delete-feature
 */
public class DeleteEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "marking files for deletion.";

    @Override
    protected void processInOnlyRequest(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // get parameters
        String fileToDelete = extractStringParameter(in, "./@" + ParameterNames.FILE_TO_DELETE);

        // log parameters
        if (getLog().isDebugEnabled()) {
            StringBuilder parameters = new StringBuilder();
            parameters.append("Parameters:\n");
            parameters.append("fileToDelete=");
            parameters.append(fileToDelete);

            getLog().debug(parameters.toString());
        }

        // validate them
        if ((fileToDelete == null)) {
            throw new IllegalArgumentException("Missing " + ParameterNames.FILE_TO_DELETE);
        }

        // execute call
        Command<?> command = getCommandFactory().getDeleteCommand(fileToDelete);
        command.execute();
    }

    @Override
    protected String getEndpointBehaviour() {
        return DeleteEndpoint.BEHAVIOR;
    }
}
