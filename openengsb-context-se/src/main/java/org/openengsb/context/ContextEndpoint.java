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
package org.openengsb.context;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.common.endpoints.ProviderEndpoint;

/**
 * @org.apache.xbean.XBean element="contextEndpoint"
 *                         description="Context Component"
 */
public class ContextEndpoint extends ProviderEndpoint {
	private static final String ID_XPATH = "./@id";
	private static final String ROOT_XPATH = ".";

	private ContextStore store = new ContextStore();

	// TODO

	@Override
	protected void processInOnly(MessageExchange exchange, NormalizedMessage in)
			throws Exception {
	}

	@Override
	protected void processInOut(MessageExchange exchange, NormalizedMessage in,
			NormalizedMessage out) throws Exception {
	}

}
