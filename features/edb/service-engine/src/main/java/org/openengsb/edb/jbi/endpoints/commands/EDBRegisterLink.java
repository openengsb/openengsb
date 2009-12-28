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

import java.util.List;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.jbi.endpoints.EdbEndpoint;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;

public class EDBRegisterLink implements EDBEndpointCommand {

    private final EDBHandler handler;
    private final Log log;

    public EDBRegisterLink(EDBHandler handler, Log log) {
        this.handler = handler;
        this.log = log;
    }

    public String execute(NormalizedMessage in) throws Exception {
        String body = null;
        try {
            List<GenericContent> links = XmlParserFunctions.parseLinkRegisterMessage(in, handler.getRepositoryBase()
                    .toString());

            if (links.size() < 1) {
                throw new EDBException("Message did not contain links to register");
            }
            handler.add(links);
            handler.commit(EdbEndpoint.DEFAULT_USER, EdbEndpoint.DEFAULT_EMAIL);
            body = XmlParserFunctions.buildLinkRegisteredBody(links);
        } catch (EDBException e) {
            body = XmlParserFunctions.buildCommitErrorBody(e.getMessage(), makeStackTraceString(e));
            this.log.info(body);
        }
        return body;
    }

    private String makeStackTraceString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
