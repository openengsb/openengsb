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
 * The Endpoint to the diff-feature
 */

public class ExportEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "exporting working-copy.";

    @Override
    protected void processInOnlyRequest(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // get parameters
        String path = extractStringParameter(in, "./@" + ParameterNames.PATH);

        // log parameters
        if (getLog().isDebugEnabled()) {
            StringBuilder parameters = new StringBuilder();
            parameters.append("Parameters:\n");
            parameters.append("path=");
            parameters.append(path);

            getLog().debug(parameters.toString());
        }

        // validate them
        if ((path == null)) {
            throw new IllegalArgumentException("Missing " + ParameterNames.PATH);
        }

        // execute call
        Command<?> command = getCommandFactory().getExportCommand(path);
        command.execute();
    }

    @Override
    protected String getEndpointBehaviour() {
        return ExportEndpoint.BEHAVIOR;
    }
}
