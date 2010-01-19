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

import org.openengsb.drools.model.LogEntry;
import org.openengsb.scm.common.ParameterNames;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.util.StringArraySerializer;
import org.w3c.dom.Node;

/**
 * The Endpoint to the log-feature
 */
public class LogEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "gathering logs.";

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        // get parameters
        String startRevision = extractStringParameter(in, "./@" + ParameterNames.START_REVISION);
        String endRevision = extractStringParameter(in, "./@" + ParameterNames.END_REVISION);
        Node filesNode = extractSingleNode(in, "./" + ParameterNames.FILES);

        // log parameters
        if (getLog().isDebugEnabled()) {
            StringBuilder parameters = new StringBuilder();
            parameters.append("Parameters:\n");
            parameters.append("startRevision=");
            parameters.append(startRevision);
            parameters.append("endRevision=");
            parameters.append(endRevision);
            parameters.append("files=");
            if (filesNode == null) {
                parameters.append("null");
            } else {
                parameters.append(StringArraySerializer.deserialize(filesNode));
            }

            getLog().debug(parameters.toString());
        }

        // validate them
        if (startRevision == null) {
            throw new IllegalArgumentException("Missing " + ParameterNames.START_REVISION);
        }
        if (startRevision == null) {
            throw new IllegalArgumentException("Missing " + ParameterNames.END_REVISION);
        }
        if (filesNode == null) {
            throw new IllegalArgumentException("Missing " + ParameterNames.FILES);
        }

        String[] files = StringArraySerializer.deserialize(filesNode);

        // execute call
        Command<LogEntry[]> command = getCommandFactory().getLogCommand(files, startRevision, endRevision);
        LogEntry[] result = command.execute();

        // TODO fix this problem which followed from changing the result type of
        // the log command
        // xml-ify result and send it back
        // out.setContent(StringMapSerializer.serialize(result, "logs"));
        // getChannel ().send (exchange);
    }

    @Override
    protected String getEndpointBehaviour() {
        return LogEndpoint.BEHAVIOR;
    }

}
