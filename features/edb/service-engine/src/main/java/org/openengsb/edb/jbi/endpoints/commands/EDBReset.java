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

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions.RequestWrapper;

public class EDBReset implements EDBEndpointCommand {
    private EDBHandler handler;
    private Log log;

    public EDBReset(EDBHandler handler, Log log) {
        this.handler = handler;
        this.log = log;
    }

    public String execute(NormalizedMessage in) throws Exception {
        String body = null;
        final RequestWrapper req = XmlParserFunctions.parseResetMessage(in);
        log.debug(String.format("Reset request %s %s received, processing now.", req.getHeadId(), req.getDepth()));
        try {
            body = XmlParserFunctions.buildResetResponseBody(handler.reset(req.getHeadId(), req.getDepth()));
        } catch (final EDBException e) {
            body = XmlParserFunctions.buildResetErrorResponseBody(e.getMessage(), e.getStackTrace().toString());
        }
        return body;
    }

}
