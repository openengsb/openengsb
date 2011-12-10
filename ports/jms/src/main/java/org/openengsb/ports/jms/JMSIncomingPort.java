/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ports.jms;

import java.util.HashMap;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openengsb.core.common.remote.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSIncomingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSIncomingPort.class);

    private static final String DISABLE_ENCRYPTION = "org.openengsb.jms.noencrypt";

    private JMSTemplateFactory factory;

    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private FilterChain filterChain;

    private String receive = "receive";

    /*
     * TODO OPENENGSB-1575 this property is kind of a hack and should be
     * replaced by proper dynamic port configuration
     */
    private FilterChain unsecureFilterChain;

    public void start() {
        simpleMessageListenerContainer = createListenerContainer(receive, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                LOGGER.trace("JMS-message recieved. Checking if the type is supported");
                if (!(message instanceof TextMessage)) {
                    LOGGER.debug("Received JMS-message is not type of text message.");
                    return;
                }
                LOGGER.trace("Received a text message and start parsing");
                TextMessage textMessage = (TextMessage) message;
                String textContent = extractTextFromMessage(textMessage);
                HashMap<String, Object> metadata = new HashMap<String, Object>();
                String result = null;
                try {
                    LOGGER.debug("starting filterchain for incoming message");
                    result = (String) getFilterChainToUse().filter(textContent, metadata);
                } catch (Exception e) {
                    LOGGER.error("an error occured when processing the filterchain", e);
                    result = ExceptionUtils.getStackTrace(e);
                }
                Destination replyQueue;
                final String correlationID;
                try {
                    correlationID = message.getJMSCorrelationID();
                    replyQueue = message.getJMSReplyTo();
                } catch (JMSException e) {
                    LOGGER.warn("error when getting destination queue or correlationid from client message: {}", e);
                    return;
                }
                if (replyQueue == null) {
                    LOGGER.warn("no replyTo destination specifyed could not send response");
                    return;
                }
                new JmsTemplate(connectionFactory).convertAndSend(replyQueue, result, new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws JMSException {
                        message.setJMSCorrelationID(correlationID);
                        return message;
                    }
                });
            }

            private String extractTextFromMessage(TextMessage textMessage) {
                try {
                    return textMessage.getText();
                } catch (JMSException e) {
                    throw new IllegalStateException("Couldn't extract text from jms message", e);
                }
            }

        });

        simpleMessageListenerContainer.start();
    }

    private SimpleMessageListenerContainer createListenerContainer(String destination, MessageListener listener) {
        SimpleMessageListenerContainer messageListenerContainer = factory.createMessageListenerContainer();
        messageListenerContainer.setPubSubNoLocal(true);
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestinationName(destination);
        messageListenerContainer.setMessageListener(listener);
        return messageListenerContainer;
    }

    public void stop() {
        if (simpleMessageListenerContainer != null) {
            simpleMessageListenerContainer.stop();
        }
    }

    /*
     * TODO OPENENGSB-1575 this property is kind of a hack and should be
     * replaced by proper dynamic port configuration
     */
    private FilterChain getFilterChainToUse() {
        if (Boolean.getBoolean(DISABLE_ENCRYPTION)) {
            return unsecureFilterChain;
        }
        return filterChain;
    }

    public void setFactory(JMSTemplateFactory factory) {
        this.factory = factory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

    public void setUnsecureFilterChain(FilterChain unsecureFilterChain) {
        this.unsecureFilterChain = unsecureFilterChain;
    }

    public void setReceive(String receive) {
        this.receive = receive;
    }
}
