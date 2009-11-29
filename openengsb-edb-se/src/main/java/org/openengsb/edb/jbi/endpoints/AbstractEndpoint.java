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

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.openengsb.edb.core.api.EDBHandlerFactory;
import org.openengsb.edb.core.api.impl.DefaultEDBHandlerFactory;

/**
 * An abstract ProviderEnpoint to be used by all endpoints that supply
 * functionality for EDB.
 * 
 */
public abstract class AbstractEndpoint extends ProviderEndpoint {

	protected EDBHandlerFactory factory;
	protected EDBEndPointConfig fullConfig;

	public EDBEndPointConfig getFullConfig() {
		return fullConfig;
	}

	public void setFullConfig(EDBEndPointConfig fullConfig) {
		this.fullConfig = fullConfig;
	}

	protected AbstractEndpoint() {
		this.factory = new DefaultEDBHandlerFactory();
		this.fullConfig = new EDBEndPointConfig();
		this.fullConfig.setLinkStorage("links");
	}

	/**
	 * @return the factory
	 */
	public EDBHandlerFactory getFactory() {
		return this.factory;
	}

	/**
	 * @param factory
	 *            the factory to set
	 */
	public void setFactory(EDBHandlerFactory factory) {
		this.factory = factory;
	}

	@Override
	public void validate() throws DeploymentException {
		return;
	}

	@Override
	protected void processInOnly(MessageExchange exchange, NormalizedMessage in)
			throws Exception {
		try {
			if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
				// call template method
				processInOnlyRequest(exchange, in);
				getLog().info("call finished");
			} else {
				getLog().warn("Exchange was not ACTIVE. Ignoring it.");
			}
		} catch (Exception exception) {
			getLog().error("Encountered an error ", exception);
			throw exception;
		}
	}

	@Override
	protected void processInOut(MessageExchange exchange, NormalizedMessage in,
			NormalizedMessage out) throws Exception {
		try {
			if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
				// call template method
				processInOutRequest(exchange, in, out);
				getLog().info("call finished");
			} else {
				getLog().warn("Exchange was not ACTIVE. Ignoring it.");
			}
		} catch (Exception exception) {
			getLog().error("ERROR occured");
			getLog().error(exception);
			exception.printStackTrace();
			throw exception;
		}
	}

	/* end ProviderEndpoint overrides */

	/* template methods and default implementations */

	/**
	 * Template method that is called from the implementation for processInOut.
	 * The default implementation just calls the super-method of processInOut
	 * which in turn throws an Exception telling the caller, that this MEP is
	 * not supported.
	 * 
	 * @param exchange
	 *            see {@link ProviderEndpoint#processInOut}
	 * @param in
	 *            see {@link ProviderEndpoint#processInOut}
	 * @param out
	 *            see {@link ProviderEndpoint#processInOut}
	 * @throws Exception
	 *             see {@link ProviderEndpoint#processInOut}
	 */
	protected void processInOutRequest(MessageExchange exchange,
			NormalizedMessage in, NormalizedMessage out) throws Exception {
		super.processInOut(exchange, in, out);
	}

	/**
	 * Template method that is called from the implementation for processInOnly.
	 * The default implementation just calls the super-method of processInOut
	 * which in turn throws an Exception telling the caller, that this MEP is
	 * not supported.
	 * 
	 * @param exchange
	 *            see {@link ProviderEndpoint#processInOut}
	 * @param in
	 *            see {@link ProviderEndpoint#processInOut}
	 * @throws Exception
	 *             see {@link ProviderEndpoint#processInOut}
	 */
	protected void processInOnlyRequest(MessageExchange exchange,
			NormalizedMessage in) throws Exception {
		super.processInOnly(exchange, in);
	}

	/* end template methods and default implementations */

	/**
	 * This method may seem not very useful, since logger is protected already
	 * and could be accessed directly. It exists to change loggers easily. I.e.
	 * to exchange the jbi-default-logger with a self-instantiated one.
	 * 
	 * @return
	 */
	protected Log getLog() {
		return this.logger;
	}

}
