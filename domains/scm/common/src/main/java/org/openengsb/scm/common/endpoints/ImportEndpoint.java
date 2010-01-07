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
import org.openengsb.scm.common.pojos.MergeResult;
import org.openengsb.scm.common.util.MergeResultSerializer;


/**
 * The Endpoint to the import-feature
 */
public class ImportEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "importing files to repository.";

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        // get parameters
        String source = extractStringParameter(in, "./@" + ParameterNames.SOURCE);
        String destination = extractStringParameter(in, "./@" + ParameterNames.DESTINATION);
        String message = extractStringParameter(in, "./@" + ParameterNames.MESSAGE);
        String author = extractStringParameter(in, "./@" + ParameterNames.AUTHOR);

        // log parameters
        if (getLog().isDebugEnabled()) {
            StringBuilder parameters = new StringBuilder();
            parameters.append("Parameters:\n");
            parameters.append("source=");
            parameters.append(source);
            parameters.append("destination=");
            parameters.append(destination);
            parameters.append("message=");
            parameters.append(message);
            parameters.append("author=");
            parameters.append(author);

            getLog().debug(parameters.toString());
        }

        // validate them
        if (source == null) {
            throw new IllegalArgumentException("Missing " + ParameterNames.SOURCE);
        }
        // destination may stay null if it wants to
        if (message == null) {
            throw new IllegalArgumentException("Missing " + ParameterNames.MESSAGE);
        }
        if (author == null) {
            throw new IllegalArgumentException("Missing " + ParameterNames.AUTHOR);
        }

        // execute call
        Command<MergeResult> command = getCommandFactory().getImportCommand(source, destination, message, author);
        MergeResult result = command.execute();

        // xml-ify result and send it back
        out.setContent(MergeResultSerializer.serialize(result));
        // getChannel ().send (exchange);
    }

    @Override
    protected String getEndpointBehaviour() {
        return ImportEndpoint.BEHAVIOR;
    }

}
