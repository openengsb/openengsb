/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.drools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.common.util.MessageUtil;
import org.apache.servicemix.drools.model.Exchange;
import org.drools.KnowledgeBase;
import org.drools.RuleBase;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.RuleBaseLoader;
import org.springframework.core.io.Resource;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="endpoint"
 */

public class DroolsEndpoint extends ProviderEndpoint {

	private RuleBase ruleBase;
	private KnowledgeBase kb;
	private Resource ruleBaseResource;
	private URL ruleBaseURL;
	private NamespaceContext namespaceContext;
	private QName defaultTargetService;
	private String defaultTargetURI;
	private Map<String, Object> globals;
	private List<Object> assertedObjects;
	private boolean autoReply;

	@SuppressWarnings("serial")
	private ConcurrentMap<String, DroolsExecutionContext> pending = new ConcurrentHashMap<String, DroolsExecutionContext>() {
		public DroolsExecutionContext remove(Object key) {
			DroolsExecutionContext context = super.remove(key);
			if (context != null) {
				// stop the execution context -- updating and disposing of any
				// working memory
				context.update();
				context.stop();
			}
			return context;
		};
	};

	public DroolsEndpoint() {
		super();
	}

	public DroolsEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
		super(component, endpoint);
	}

	public DroolsEndpoint(ServiceUnit su, QName service, String endpoint) {
		super(su, service, endpoint);
	}

	/**
	 * @return the ruleBase
	 */
	public RuleBase getRuleBase() {
		return ruleBase;
	}

	/**
	 * @param ruleBase
	 *            the ruleBase to set
	 */
	public void setRuleBase(RuleBase ruleBase) {
		this.ruleBase = ruleBase;
	}

	/**
	 * @return the ruleBaseResource
	 */
	public Resource getRuleBaseResource() {
		return ruleBaseResource;
	}

	/**
	 * @param ruleBaseResource
	 *            the ruleBaseResource to set
	 */
	public void setRuleBaseResource(Resource ruleBaseResource) {
		this.ruleBaseResource = ruleBaseResource;
	}

	/**
	 * @return the ruleBaseURL
	 */
	public URL getRuleBaseURL() {
		return ruleBaseURL;
	}

	/**
	 * @param ruleBaseURL
	 *            the ruleBaseURL to set
	 */
	public void setRuleBaseURL(URL ruleBaseURL) {
		this.ruleBaseURL = ruleBaseURL;
	}

	/**
	 * @return the namespaceContext
	 */
	public NamespaceContext getNamespaceContext() {
		return namespaceContext;
	}

	/**
	 * @param namespaceContext
	 *            the namespaceContext to set
	 */
	public void setNamespaceContext(NamespaceContext namespaceContext) {
		this.namespaceContext = namespaceContext;
	}

	/**
	 * @return the variables
	 */
	public Map<String, Object> getGlobals() {
		return globals;
	}

	/**
	 * @param variables
	 *            the variables to set
	 */
	public void setGlobals(Map<String, Object> variables) {
		this.globals = variables;
	}

	/**
	 * Will this endpoint automatically reply to any exchanges not handled by
	 * the Drools rulebase?
	 * 
	 * @return <code>true</code> if the endpoint replies to any unanswered
	 *         exchanges
	 */
	public boolean isAutoReply() {
		return autoReply;
	}

	/**
	 * Set auto-reply to <code>true</code> to ensure that every exchange is
	 * being replied to. This way, you can avoid having to end every Drools rule
	 * with jbi.answer()
	 * 
	 * @param autoReply
	 *            <code>true</code> for auto-replying on incoming exchanges
	 */
	public void setAutoReply(boolean autoReply) {
		this.autoReply = autoReply;
	}

	public void validate() throws DeploymentException {
		super.validate();
		if (ruleBase == null && ruleBaseResource == null && ruleBaseURL == null) {
			throw new DeploymentException(
					"Property ruleBase, ruleBaseResource or ruleBaseURL must be set");
		}
	}

	public void start() throws Exception {
		super.start();
		if (ruleBase == null) {
			InputStream is = null;
			try {
				if (ruleBaseResource != null) {
					is = ruleBaseResource.getInputStream();
				} else if (ruleBaseURL != null) {
					is = ruleBaseURL.openStream();
				} else {
					throw new IllegalArgumentException(
							"Property ruleBase, ruleBaseResource "
									+ "or ruleBaseURL must be set");
				}
				RuleBaseLoader loader = RuleBaseLoader.getInstance();
				ruleBase = loader.loadFromReader(new InputStreamReader(is));
			} catch (Exception e) {
				throw new JBIException(e);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.endpoints.ProviderEndpoint#process(
	 * javax.jbi.messaging.MessageExchange,
	 * javax.jbi.messaging.NormalizedMessage)
	 */
	public void process(MessageExchange exchange) throws Exception {
		if (exchange.getRole() == Role.PROVIDER) {
			handleProviderExchange(exchange);
		} else {
			handleConsumerExchange(exchange);
		}
	}

	/*
	 * Handle a consumer exchange
	 */
	private void handleConsumerExchange(MessageExchange exchange)
			throws MessagingException {
		String correlation = (String) exchange
				.getProperty(DroolsComponent.DROOLS_CORRELATION_ID);
		DroolsExecutionContext drools = pending.get(correlation);
		if (drools != null) {
			MessageExchange original = drools.getExchange();
			if (exchange.getStatus() == ExchangeStatus.DONE) {
				done(original);
			} else if (exchange.getStatus() == ExchangeStatus.ERROR) {
				fail(original, exchange.getError());
			} else {
				if (exchange.getFault() != null) {
					MessageUtil.transferFaultToFault(exchange, original);
				} else {
					MessageUtil.transferOutToOut(exchange, original);
				}
				// TODO: remove this sendSync() and replace by a send()
				// TODO: there is a need to store the exchange and send the DONE
				// TODO: when the original comes back
				sendSync(original);
				done(exchange);
			}
		} else {
			logger.debug("No pending exchange found for " + correlation
					+ ", no additional rules will be triggered");
		}
	}

	private void handleProviderExchange(MessageExchange exchange)
			throws Exception {
		if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
			drools(exchange);
		} else {
			// must be a DONE/ERROR so removing any pending contexts
			pending.remove(exchange.getExchangeId());
		}
	}

	public static String getCorrelationId(MessageExchange exchange) {
		Object correlation = exchange.getProperty(JbiConstants.CORRELATION_ID);
		if (correlation == null) {
			return exchange.getExchangeId();
		} else {
			return correlation.toString();
		}
	}

	protected void drools(MessageExchange exchange) throws Exception {
		DroolsExecutionContext drools = startDroolsExecutionContext(exchange);
		if (drools.getRulesFired() < 1) {
			if (getDefaultTargetService() == null) {
				fail(
						exchange,
						new Exception(
								"No rules have handled the exchange. Check your rule base."));
			} else {
				drools.getHelper().route(getDefaultRouteURI());
			}
		} else {
			// the exchange has been answered or faulted by the drools endpoint
			if (drools.isExchangeHandled() && exchange instanceof InOnly) {
				// only removing InOnly
				pending.remove(exchange.getExchangeId());
			}
			if (!drools.isExchangeHandled() && autoReply) {
				reply(exchange, drools);
			}
		}
	}

	private void reply(MessageExchange exchange, DroolsExecutionContext drools)
			throws Exception {
		Fault fault = exchange.getFault();
		if (fault != null) {
			drools.getHelper().fault(fault.getContent());
		} else if (isOutCapable(exchange)) {
			NormalizedMessage message = exchange
					.getMessage(Exchange.OUT_MESSAGE);
			if (message == null) {
				// send back the 'in' message if no 'out' message is available
				message = exchange.getMessage(Exchange.IN_MESSAGE);
			}
			drools.getHelper().answer(message.getContent());
		} else if (exchange instanceof InOnly) {
			// just send back the done
			done(exchange);
		}
	}

	private boolean isOutCapable(MessageExchange exchange) {
		return exchange instanceof InOptionalOut || exchange instanceof InOut;
	}

	private DroolsExecutionContext startDroolsExecutionContext(
			MessageExchange exchange) {
		DroolsExecutionContext drools = new DroolsExecutionContext(this,
				exchange);
		pending.put(exchange.getExchangeId(), drools);
		drools.start();
		return drools;
	}

	public QName getDefaultTargetService() {
		return defaultTargetService;
	}

	public void setDefaultTargetService(QName defaultTargetService) {
		this.defaultTargetService = defaultTargetService;
	}

	public String getDefaultTargetURI() {
		return defaultTargetURI;
	}

	public void setDefaultTargetURI(String defaultTargetURI) {
		this.defaultTargetURI = defaultTargetURI;
	}

	public List<Object> getAssertedObjects() {
		return assertedObjects;
	}

	public void setAssertedObjects(List<Object> assertedObjects) {
		this.assertedObjects = assertedObjects;
	}

	public void addDrlRule(Reader reader) throws Exception {
		PackageBuilder pb = new PackageBuilder();
		pb.addPackageFromDrl(reader);
		if (!pb.hasErrors()) {
			ruleBase.addPackage(pb.getPackage());
		}
	}

	public String getDefaultRouteURI() {
		if (defaultTargetURI != null) {
			return defaultTargetURI;
		} else if (defaultTargetService != null) {
			String nsURI = defaultTargetService.getNamespaceURI();
			String sep = (nsURI.indexOf("/") > 0) ? "/" : ":";
			return "service:" + nsURI + sep
					+ defaultTargetService.getLocalPart();
		} else {
			return null;
		}
	}

	@Override
	protected void send(MessageExchange me) throws MessagingException {
		if (me.getStatus() != ExchangeStatus.ACTIVE) {
			// must be a DONE/ERROR so removing any pending contexts
			pending.remove(me.getExchangeId());
		}
		super.send(me);
	}
}
