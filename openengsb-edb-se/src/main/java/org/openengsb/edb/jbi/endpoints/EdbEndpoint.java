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
package org.openengsb.edb.jbi.endpoints;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.jbi.endpoints.commands.EDBCommit;
import org.openengsb.edb.jbi.endpoints.commands.EDBEndpointCommand;
import org.openengsb.edb.jbi.endpoints.commands.EDBQuery;
import org.openengsb.edb.jbi.endpoints.commands.EDBReset;

/**
 * @org.apache.xbean.XBean element="edb" The Endpoint to the commit-feature
 * 
 */
public class EdbEndpoint extends AbstractEndpoint {

    /*
     * Operations
     * 
     * Strings to identify an operation. {@link
     * javax.jbi.messaging.MessageExchange} requires a QName as operation.
     * 
     * setOperation(new QName(OPERATION_COMMIT))
     * 
     * The namespace is ignored in the operation-check
     */

    public static final String DEFAULT_USER = "EDB";
    public static final String DEFAULT_EMAIL = "EDB@engsb.ifs.tuwien.ac.at";

    public static final String COMMIT_OPERATION_TAG_NAME = "operation";
    public static final String QUERY_ELEMENT_NAME = "query";
    public static final int DEFAULT_DEPTH = 1;

    // should be set via spring ?
    private Map<EDBOperationType, EDBEndpointCommand> commands;

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        getLog().info("init handler from factory");

        EDBHandler handler = this.fullConfig.getFactory().loadDefaultRepository();
        EDBHandler linksHandler = this.fullConfig.getFactory().loadRepository(this.fullConfig.getLinkStorage());

        // see issue #179
        init(handler, linksHandler);

        getLog().info("parsing message");
        /*
         * Only check the local part. Don't care about the namespace of the
         * operation
         */
        EDBOperationType op = XmlParserFunctions.getMessageType(in);// exchange.getOperation().getLocalPart();
        String body = null;

        body = this.commands.get(op).execute(in);

        body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><acmResponseMessage><body>" + body
                + "</body></acmResponseMessage>";
        Source response = new StringSource(body);
        this.logger.info(body);
        out.setContent(response);
        getChannel().send(exchange);
    }

    /**
     * see issue 179
     */
    private void init(EDBHandler handler, EDBHandler linksHandler) {
        this.commands = new HashMap<EDBOperationType, EDBEndpointCommand>();
        this.commands.put(EDBOperationType.COMMIT, new EDBCommit(handler, this.logger));
        this.commands.put(EDBOperationType.QUERY, new EDBQuery(handler, this.logger));
        this.commands.put(EDBOperationType.RESET, new EDBReset(handler, this.logger));
    }

}
