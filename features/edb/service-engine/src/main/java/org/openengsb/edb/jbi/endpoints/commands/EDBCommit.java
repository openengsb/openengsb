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
package org.openengsb.edb.jbi.endpoints.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.entities.OperationType;
import org.openengsb.edb.jbi.endpoints.EdbEndpoint;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions.ContentWrapper;

public class EDBCommit implements EDBEndpointCommand {

    private final EDBHandler handler;
    private final Log log;

    public EDBCommit(EDBHandler handler, Log log) {
        this.handler = handler;
        this.log = log;
    }

    public CommandResult execute(NormalizedMessage in) throws Exception {
        String body = null;
        String userName = "author";
        try {
            List<ContentWrapper> contentWrappers = XmlParserFunctions.parseCommitMessage(in, handler
                    .getRepositoryBase().toString());
            if (contentWrappers.size() < 1) {
                throw new EDBException("Message did not contain files to commit");
            }
            final List<GenericContent> listAdd = new ArrayList<GenericContent>();
            final List<GenericContent> listRemove = new ArrayList<GenericContent>();

            userName = extractUserInfo(contentWrappers);

            for (final ContentWrapper content : contentWrappers) {
                // update search index
                if (content.getOperation() == OperationType.UPDATE) {
                    listAdd.add(content.getContent());
                } else if (content.getOperation() == OperationType.DELETE) {
                    listRemove.add(content.getContent());
                }
            }

            handler.add(listAdd);
            handler.remove(listRemove);

            String commitId = handler.commit(userName, EdbEndpoint.DEFAULT_EMAIL);
            body = XmlParserFunctions.buildCommitResponseBody(contentWrappers, commitId);
        } catch (EDBException e) {
            body = XmlParserFunctions.buildCommitErrorResponseBody(e.getMessage(), makeStackTraceString(e));
            this.log.info(body);
        }
        CommandResult result = new CommandResult();
        result.responseString = body;
        result.eventAttributes.put("author", userName);
        return result;
    }

    private static String extractUserInfo(List<ContentWrapper> contentWrappers) {
        String result = EdbEndpoint.DEFAULT_USER;
        ContentWrapper content = contentWrappers.get(0);
        if (!(content.getUser() == null)) {
            if (!content.getUser().equals("")) {
                result = content.getUser();
                contentWrappers.remove(0);
            }
        }
        return result;
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
