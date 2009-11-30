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
package org.openengsb.edb.jbi.endpoints.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;

public class EDBRequestLink implements EDBEndpointCommand {

    private final EDBHandler handler;
    private final Log log;

    public EDBRequestLink(EDBHandler handler, Log log) {
        this.handler = handler;
        this.log = log;
    }

    @Override
    public String execute(NormalizedMessage in) throws Exception {

        String body = null;
        final String term = XmlParserFunctions.parseLinkRequestMessage(in);
        List<GenericContent> foundLinkTargets = new ArrayList<GenericContent>();
        log.debug(term + " link request received.");
        try {

            final List<GenericContent> result = handler.query(term, false);
            foundLinkTargets.addAll(result);

        } catch (final EDBException e) {
            // TODO build error message
            e.printStackTrace();
            foundLinkTargets = new ArrayList<GenericContent>();
        }
        body = XmlParserFunctions.buildLinkRequestedBody(foundLinkTargets);
        return body;
    }

}
