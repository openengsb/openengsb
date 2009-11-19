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
package org.apache.servicemix.jms.endpoints;

import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ConsumerEndpoint;
import org.apache.servicemix.jms.endpoints.AbstractJmsMarshaler;
import org.apache.servicemix.jms.endpoints.DefaultConsumerMarshaler;
import org.apache.servicemix.jms.endpoints.DestinationChooser;
import org.apache.servicemix.jms.endpoints.JmsConsumerMarshaler;
import org.apache.servicemix.jms.endpoints.JmsConsumerMarshaler.JmsContext;
import org.apache.servicemix.store.Store;
import org.apache.servicemix.store.StoreFactory;
import org.apache.servicemix.store.memory.MemoryStoreFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.JmsTemplate102;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.listener.adapter.ListenerExecutionFailedException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

/**
 * The base class for Spring-based JMS consumer endpoints.
 */
public abstract class AbstractConsumerEndpoint extends ConsumerEndpoint {

    protected static final String PROP_JMS_CONTEXT = JmsContext.class.getName();

    private JmsConsumerMarshaler marshaler = new DefaultConsumerMarshaler();
    private boolean synchronous = true;
    private DestinationChooser destinationChooser;
    private DestinationResolver destinationResolver = new DynamicDestinationResolver();
    private boolean pubSubDomain;
    private ConnectionFactory connectionFactory;
    private JmsTemplate template;

    // Reply properties
    private Boolean useMessageIdInResponse;
    private Destination replyDestination;
    private String replyDestinationName;
    private boolean replyExplicitQosEnabled;
    private int replyDeliveryMode = Message.DEFAULT_DELIVERY_MODE;
    private int replyPriority = Message.DEFAULT_PRIORITY;
    private long replyTimeToLive = Message.DEFAULT_TIME_TO_LIVE;
    private Map<String, Object> replyProperties;

    private boolean stateless;
    private StoreFactory storeFactory;
    private Store store;
    private boolean jms102;

    public AbstractConsumerEndpoint() {
        super();
    }

    public AbstractConsumerEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public AbstractConsumerEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    /**
     * @return the destinationChooser
     */
    public DestinationChooser getDestinationChooser() {
        return this.destinationChooser;
    }

    /**
     * Specifies a class implementing logic for choosing reply destinations.
     * 
     * @param destinationChooser the destinationChooser to set
     */
    public void setDestinationChooser(DestinationChooser destinationChooser) {
        this.destinationChooser = destinationChooser;
    }

    /**
     * @return the replyDeliveryMode
     */
    public int getReplyDeliveryMode() {
        return this.replyDeliveryMode;
    }

    /**
     * Specifies the JMS delivery mode used for the reply. Defaults to 2(
     * <code>PERSISTENT</code>).
     * 
     * @param replyDeliveryMode the JMS delivery mode
     */
    public void setReplyDeliveryMode(int replyDeliveryMode) {
        this.replyDeliveryMode = replyDeliveryMode;
    }

    /**
     * @return the replyDestination
     */
    public Destination getReplyDestination() {
        return this.replyDestination;
    }

    /**
     * Specifies the JMS <code>Destination</code> for the replies. If this value
     * is not set the endpoint will use the <code>destinationChooser</code>
     * property or the <code>replyDestinationName</code> property to determine
     * the desitination to use.
     * 
     * @param replyDestination the JMS destination for replies
     */
    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    /**
     * @return the replyDestinationName
     */
    public String getReplyDestinationName() {
        return this.replyDestinationName;
    }

    /**
     * Specifies the name of the JMS destination to use for the reply. The
     * actual JMS destination is resolved using the
     * <code>DestinationResolver</code> specified by the
     * <code>.destinationResolver</code> property.
     * 
     * @param replyDestinationName the name of the reply destination
     */
    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }

    /**
     * @return the replyExplicitQosEnabled
     */
    public boolean isReplyExplicitQosEnabled() {
        return this.replyExplicitQosEnabled;
    }

    /**
     * Specifies if the QoS values specified for the endpoint are explicitly
     * used when the reply is sent. The default is <code>false</code>.
     * 
     * @param replyExplicitQosEnabled should the QoS values be sent?
     */
    public void setReplyExplicitQosEnabled(boolean replyExplicitQosEnabled) {
        this.replyExplicitQosEnabled = replyExplicitQosEnabled;
    }

    /**
     * @return the replyPriority
     */
    public int getReplyPriority() {
        return this.replyPriority;
    }

    /**
     * Specifies the JMS message priority of the reply. Defaults to 4.
     * 
     * @param replyPriority the reply's priority
     */
    public void setReplyPriority(int replyPriority) {
        this.replyPriority = replyPriority;
    }

    /**
     * @return the replyProperties
     */
    public Map<String, Object> getReplyProperties() {
        return this.replyProperties;
    }

    /**
     * Specifies custom properties to be placed in the reply's JMS header.
     * 
     * @param replyProperties the properties to set
     */
    public void setReplyProperties(Map<String, Object> replyProperties) {
        this.replyProperties = replyProperties;
    }

    /**
     * @return the replyTimeToLive
     */
    public long getReplyTimeToLive() {
        return this.replyTimeToLive;
    }

    /**
     * Specifies the number of milliseconds the reply message is valid. The
     * default is unlimited.
     * 
     * @param replyTimeToLive the number of milliseonds the message lives
     */
    public void setReplyTimeToLive(long replyTimeToLive) {
        this.replyTimeToLive = replyTimeToLive;
    }

    /**
     * @return the useMessageIdInResponse
     */
    public Boolean getUseMessageIdInResponse() {
        return this.useMessageIdInResponse;
    }

    /**
     * Specifies if the request message's ID is used as the reply's correlation
     * ID. The default behavior is to use the request's correlation ID. Setting
     * this to <code>true</code> means the request's message ID will be used
     * instead.
     * 
     * @param useMessageIdInResponse use the request's message ID as the reply'e
     *        correlation ID?
     */
    public void setUseMessageIdInResponse(Boolean useMessageIdInResponse) {
        this.useMessageIdInResponse = useMessageIdInResponse;
    }

    /**
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    /**
     * Specifies the <code>ConnectionFactory</code> used by the endpoint.
     * 
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return this.pubSubDomain;
    }

    /**
     * Specifies if the destination is a topic. <code>true</code> means the
     * destination is a topic. <code>false</code> means the destination is a
     * queue.
     * 
     * @param pubSubDomain the destination is a topic?
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    /**
     * @return the destinationResolver
     */
    public DestinationResolver getDestinationResolver() {
        return this.destinationResolver;
    }

    /**
     * Specifies the class implementing logic for converting strings into
     * destinations. The default is <code>DynamicDestinationResolver</code>.
     * 
     * @param destinationResolver the destination resolver implementation
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    /**
     * @return the marshaler
     */
    public JmsConsumerMarshaler getMarshaler() {
        return this.marshaler;
    }

    /**
     * Specifies the class implementing the message marshaler. The message
     * marshaller is responsible for marshalling and unmarshalling JMS messages.
     * The default is <code>DefaultConsumerMarshaler</code>.
     * 
     * @param marshaler the marshaler implementation
     */
    public void setMarshaler(JmsConsumerMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return the synchronous
     */
    public boolean isSynchronous() {
        return this.synchronous;
    }

    /**
     * Specifies if the consumer will block while waiting for a response. This
     * means the consumer can only process one message at a time. Defaults to
     * <code>true</code>.
     * 
     * @param synchronous the consumer blocks?
     */
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isStateless() {
        return this.stateless;
    }

    /**
     * Specifies if the consumer retains state information about the message
     * exchange while it is in process.
     * 
     * @param stateless the consumer retains state?
     */
    public void setStateless(boolean stateless) {
        this.stateless = stateless;
    }

    public Store getStore() {
        return this.store;
    }

    /**
     * Specifies the persistent store used to store JBI exchanges that are
     * waiting to be processed. The store will be automatically created if not
     * set and the endpoint's <code>stateless</code> property is set to
     * <code>false</code>.
     * 
     * @param store the <code>Store</code> object
     */
    public void setStore(Store store) {
        this.store = store;
    }

    public StoreFactory getStoreFactory() {
        return this.storeFactory;
    }

    /**
     * Specifies the store factory used to create the store. If none is set and
     * the endpoint's <code>stateless</code> property is set to
     * <code>false</code>, a {@link MemoryStoreFactory} will be created and used
     * instead.
     * 
     * @param storeFactory the <code>StoreFactory</code> object
     */
    public void setStoreFactory(StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
    }

    @Override
    public String getLocationURI() {
        // TODO: Need to return a real URI
        return getService() + "#" + getEndpoint();
    }

    @Override
    public synchronized void activate() throws Exception {
        super.activate();
        if (this.template == null) {
            if (isJms102()) {
                this.template = new JmsTemplate102(getConnectionFactory(), isPubSubDomain());
            } else {
                this.template = new JmsTemplate(getConnectionFactory());
            }
        }
        if (this.store == null && !this.stateless) {
            if (this.storeFactory == null) {
                this.storeFactory = new MemoryStoreFactory();
            }
            this.store = this.storeFactory.open(getService().toString() + getEndpoint());
        }
    }

    @Override
    public synchronized void deactivate() throws Exception {
        if (this.store != null) {
            if (this.storeFactory != null) {
                this.storeFactory.close(this.store);
            }
            this.store = null;
        }
        super.deactivate();
    }

    @Override
    public void process(MessageExchange exchange) throws Exception {
        JmsContext context;
        if (this.stateless) {
            context = (JmsContext) exchange.getProperty(PROP_JMS_CONTEXT);
        } else {
            context = (JmsContext) this.store.load(exchange.getExchangeId());
        }
        processExchange(exchange, null, context);
    }

    protected void processExchange(final MessageExchange exchange, final Session session, final JmsContext context)
            throws Exception {
        if (exchange instanceof InOnly) {
            if (ExchangeStatus.ERROR.equals(exchange.getStatus()) && this.marshaler instanceof DefaultConsumerMarshaler
                    && ((DefaultConsumerMarshaler) this.marshaler).isRollbackOnError()) {

                throw exchange.getError();
            }
            // For InOnly exchanges, ignore DONE exchanges or those where
            // isRollbackOnError is false
            return;
        }

        // Create session if needed
        if (session == null) {
            this.template.execute(new SessionCallback() {
                public Object doInJms(Session session) throws JMSException {
                    try {
                        processExchange(exchange, session, context);
                    } catch (Exception e) {
                        throw new ListenerExecutionFailedException("Exchange processing failed", e);
                    }
                    return null;
                }
            });
            return;
        }
        // Handle exchanges
        Message msg = null;
        Destination dest = null;
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            if (exchange.getFault() != null) {
                msg = this.marshaler.createFault(exchange, exchange.getFault(), session, context);
                dest = getReplyDestination(exchange, exchange.getFault(), session, context);
            } else if (exchange.getMessage("out") != null) {
                msg = this.marshaler.createOut(exchange, exchange.getMessage("out"), session, context);
                dest = getReplyDestination(exchange, exchange.getMessage("out"), session, context);
            }
            if (msg == null) {
                throw new IllegalStateException("Unable to send back answer or fault");
            }
            setCorrelationId(context.getMessage(), msg);
            try {
                send(msg, session, dest);
                done(exchange);
            } catch (Exception e) {
                fail(exchange, e);
                throw e;
            }
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            Exception error = exchange.getError();
            if (error == null) {
                error = new JBIException("Exchange in ERROR state, but no exception provided");
            }
            msg = this.marshaler.createError(exchange, error, session, context);
            dest = getReplyDestination(exchange, error, session, context);
            setCorrelationId(context.getMessage(), msg);
            send(msg, session, dest);
        } else if (exchange.getStatus() == ExchangeStatus.DONE) {
            msg = session.createMessage();
            msg.setBooleanProperty(AbstractJmsMarshaler.DONE_JMS_PROPERTY, true);
            dest = getReplyDestination(exchange, null, session, context);
            setCorrelationId(context.getMessage(), msg);
            send(msg, session, dest);
        } else {
            throw new IllegalStateException("Unrecognized exchange status");
        }
    }

    protected void send(Message msg, Session session, Destination dest) throws JMSException {
        MessageProducer producer;
        if (isJms102()) {
            if (isPubSubDomain()) {
                producer = ((TopicSession) session).createPublisher((Topic) dest);
            } else {
                producer = ((QueueSession) session).createSender((Queue) dest);
            }
        } else {
            producer = session.createProducer(dest);
        }
        try {
            if (this.replyProperties != null) {
                for (Map.Entry<String, Object> e : this.replyProperties.entrySet()) {
                    msg.setObjectProperty(e.getKey(), e.getValue());
                }
            }
            if (isJms102()) {
                if (isPubSubDomain()) {
                    if (this.replyExplicitQosEnabled) {
                        ((TopicPublisher) producer).publish(msg, this.replyDeliveryMode, this.replyPriority,
                                this.replyTimeToLive);
                    } else {
                        ((TopicPublisher) producer).publish(msg);
                    }
                } else {
                    if (this.replyExplicitQosEnabled) {
                        ((QueueSender) producer).send(msg, this.replyDeliveryMode, this.replyPriority,
                                this.replyTimeToLive);
                    } else {
                        ((QueueSender) producer).send(msg);
                    }
                }
            } else {
                if (this.replyExplicitQosEnabled) {
                    producer.send(msg, this.replyDeliveryMode, this.replyPriority, this.replyTimeToLive);
                } else {
                    producer.send(msg);
                }
            }
        } finally {
            JmsUtils.closeMessageProducer(producer);
        }
    }

    protected void onMessage(Message jmsMessage, Session session) throws JMSException {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Received: " + jmsMessage);
        }

        JmsContext context = null;
        MessageExchange exchange = null;

        try {
            context = this.marshaler.createContext(jmsMessage);
            exchange = this.marshaler.createExchange(context, getContext());
            configureExchangeTarget(exchange);
            if (this.synchronous) {
                try {
                    sendSync(exchange);
                } catch (Exception e) {
                    handleException(exchange, e, session, context);
                }
                processExchange(exchange, session, context);
            } else {
                if (this.stateless) {
                    exchange.setProperty(PROP_JMS_CONTEXT, context);
                } else {
                    this.store.store(exchange.getExchangeId(), context);
                }
                boolean success = false;
                try {
                    send(exchange);
                    success = true;
                } catch (Exception e) {
                    handleException(exchange, e, session, context);
                } finally {
                    if (!success && !this.stateless) {
                        this.store.load(exchange.getExchangeId());
                    }
                }
            }
        } catch (Exception e) {
            try {
                handleException(exchange, e, session, context);
            } catch (Exception e1) {
                throw (JMSException) new JMSException("Error sending JBI exchange").initCause(e);
            }
        }
    }

    protected Destination getReplyDestination(MessageExchange exchange, Object message, Session session,
            JmsContext context) throws JMSException {
        // If a JMS ReplyTo property is set, use it
        if (context.getMessage().getJMSReplyTo() != null) {
            return context.getMessage().getJMSReplyTo();
        }
        Object dest = null;
        // Let the destinationChooser a chance to choose the destination
        if (this.destinationChooser != null) {
            dest = this.destinationChooser.chooseDestination(exchange, message);
        }
        // Default to replyDestination / replyDestinationName properties
        if (dest == null) {
            dest = this.replyDestination;
        }
        if (dest == null) {
            dest = this.replyDestinationName;
        }
        // Resolve destination if needed
        if (dest instanceof Destination) {
            return (Destination) dest;
        } else if (dest instanceof String) {
            return this.destinationResolver.resolveDestinationName(session, (String) dest, isPubSubDomain());
        }
        throw new IllegalStateException("Unable to choose destination for exchange " + exchange);
    }

    protected void setCorrelationId(Message query, Message reply) throws Exception {
        if (this.useMessageIdInResponse == null) {
            if (query.getJMSCorrelationID() != null) {
                reply.setJMSCorrelationID(query.getJMSCorrelationID());
            } else if (query.getJMSMessageID() != null) {
                reply.setJMSCorrelationID(query.getJMSMessageID());
            } else {
                throw new IllegalStateException("No JMSCorrelationID or JMSMessageID set on query message");
            }
        } else if (this.useMessageIdInResponse.booleanValue()) {
            if (query.getJMSMessageID() != null) {
                reply.setJMSCorrelationID(query.getJMSMessageID());
            } else {
                throw new IllegalStateException("No JMSMessageID set on query message");
            }
        } else {
            if (query.getJMSCorrelationID() != null) {
                reply.setJMSCorrelationID(query.getJMSCorrelationID());
            } else {
                throw new IllegalStateException("No JMSCorrelationID set on query message");
            }
        }
    }

    protected void handleException(MessageExchange exchange, Exception error, Session session, JmsContext context)
            throws Exception {
        // For InOnly, the consumer does not expect any response back, so
        // just rethrow it and let the fault behavior
        if (exchange instanceof InOnly) {
            throw error;
        }
        // Check if the exception should lead to an error back
        if (treatExceptionAsFault(error)) {
            sendError(exchange, error, session, context);
        } else {
            throw error;
        }
    }

    protected boolean treatExceptionAsFault(Exception error) {
        return error instanceof SecurityException;
    }

    protected void sendError(final MessageExchange exchange, final Exception error, Session session,
            final JmsContext context) throws Exception {
        // Create session if needed
        if (session == null) {
            this.template.execute(new SessionCallback() {
                public Object doInJms(Session session) throws JMSException {
                    try {
                        sendError(exchange, error, session, context);
                    } catch (Exception e) {
                        throw new ListenerExecutionFailedException("Exchange processing failed", e);
                    }
                    return null;
                }
            });
            return;
        }
        Message msg = this.marshaler.createError(exchange, error, session, context);
        Destination dest = getReplyDestination(exchange, error, session, context);
        setCorrelationId(context.getMessage(), msg);
        send(msg, session, dest);
    }

    /**
     * @return the jms102
     */
    public boolean isJms102() {
        return this.jms102;
    }

    /**
     * Specifies if the consumer uses JMS 1.0.2 compliant APIs. Defaults to
     * <code>false</code>.
     * 
     * @param jms102 consumer is JMS 1.0.2 compliant?
     * @org.apache.xbean.Property description=
     *                            "Specifies if the consumer uses JMS 1.0.2 compliant APIs. Defaults to
     *                            <code>false</code>."
     */
    public void setJms102(boolean jms102) {
        this.jms102 = jms102;
    }

}
