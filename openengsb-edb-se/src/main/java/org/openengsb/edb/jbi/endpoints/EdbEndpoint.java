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
	/**
	 * String to identify a commit-operation in a
	 * {@link javax.jbi.messaging.MessageExchange}
	 */
	public static final String OPERATION_COMMIT = "commit";
	/**
	 * String to identify a query-operation in a
	 * {@link javax.jbi.messaging.MessageExchange}
	 */
	public static final String OPERATION_QUERY = "query";
	/**
	 * String to identify a reset-operation in a
	 * {@link javax.jbi.messaging.MessageExchange}
	 */
	public static final String OPERATION_RESET = "reset";

	public static final String DEFAULT_USER = "EDB";
	public static final String DEFAULT_EMAIL = "EDB@engsb.ifs.tuwien.ac.at";

	public static final String COMMIT_OPERATION_TAG_NAME = "operation";
	public static final String QUERY_ELEMENT_NAME = "query";
	public static final int DEFAULT_DEPTH = 1;
	
	// should be set via spring ?
	private Map<String, EDBEndpointCommand> commands;

	@Override
	protected void processInOutRequest(MessageExchange exchange,
			NormalizedMessage in, NormalizedMessage out) throws Exception {
		getLog().info("init handler from factory");

		EDBHandler handler = this.factory.loadDefaultRepository();
		
		// see issue #179
		init(handler);
		
		getLog().info("parsing message");
		/*
		 * Only check the local part. Don't care about the namespace of the
		 * operation
		 */
		String op = XmlParserFunctions.getMessageType(in);// exchange.getOperation().getLocalPart();
		String body = null;

		body = this.commands.get(op).execute(in);
		
		body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><acmResponseMessage><body>"
				+ body + "</body></acmResponseMessage>";
		Source response = new StringSource(body);
		this.logger.info(body);
		out.setContent(response);
		getChannel().send(exchange);
	}
	
	/**
	 * see issue 179
	 */
	private void init(EDBHandler handler){
		this.commands = new HashMap<String, EDBEndpointCommand>();
		this.commands.put(OPERATION_COMMIT, new EDBCommit(handler, logger));
		this.commands.put(OPERATION_QUERY, new EDBQuery(handler, logger));
		this.commands.put(OPERATION_RESET, new EDBReset(handler, logger));
	}
}
