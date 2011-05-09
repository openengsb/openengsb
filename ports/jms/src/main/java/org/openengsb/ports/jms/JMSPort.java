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

import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.marshaling.RequestMapping;
import org.openengsb.core.common.marshaling.ReturnMapping;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JMSPort implements OutgoingPort {

    private static final String RECEIVE = "receive";

    private JMSTemplateFactory factory;

    private ConnectionFactory connectionFactory;

    private RequestHandler requestHandler;
    private AuthenticationManager authenticationManager;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    @Override
    public void send(String destination, MethodCall call) {
        sendMessage(destination, call);
    }

    @Override
    public MethodReturn sendSync(String destination, MethodCall call) {
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        RequestMapping mapping = new RequestMapping(call);
        mapping.setAnswer(true);
        mapping.setCallId(currentTimeMillis);
        sendMessage(destination, mapping);
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.setReceiveTimeout(3000);
        Object receiveAndConvert = createJMSTemplate.receiveAndConvert(currentTimeMillis);
        if (receiveAndConvert == null) {
            throw new RuntimeException("JMS Receive Timeout reached");
        }
        if (receiveAndConvert instanceof String) {
            return createMethodReturn((String) receiveAndConvert);
        } else {
            throw new IllegalStateException("Message has to be of Type TextMessage and parseable into a String");
        }
    }

    private MethodReturn createMethodReturn(String receiveAndConvert) {
        try {
            return ReturnMapping.createFromMessage(receiveAndConvert);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private JmsTemplate createJMSTemplate(String destination) {
        DestinationUrl destinationUrl = DestinationUrl.createDestinationUrl(destination);
        return factory.createJMSTemplate(destinationUrl);
    }

    private void sendMessage(String destination, MethodCall call) {
        String answer;
        try {
            answer = new RequestMapping(call).convertToMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JmsTemplate template = createJMSTemplate(destination);
        template.convertAndSend(answer);
    }

    public void setRequestHandler(RequestHandler handler) {
        requestHandler = handler;
    }

    public void start() {
        simpleMessageListenerContainer = createListenerContainer(RECEIVE, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        ensureAuthentication();
                        RequestMapping readValue = RequestMapping.createFromMessage(textMessage.getText());
                        readValue.resetArgs();
                        ContextHolder.get().setCurrentContextId(readValue.getMetaData().get("contextId"));
                        MethodReturn handleCall = requestHandler.handleCall(readValue);
                        ReturnMapping returnMapping = new ReturnMapping(handleCall);
                        returnMapping.setClassName(returnMapping.getArg().getClass().getName());
                        String answer = returnMapping.convertToMessage();
                        if (readValue.isAnswer()) {
                            new JmsTemplate(connectionFactory).convertAndSend(readValue.getCallId(), answer);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        simpleMessageListenerContainer.start();
    }

    /**
     * FIXME [OPENENGSB-1226] as soon as authentication over JMS is properly implemented this hack needs to be removed as
     * it grants universal access.
     */
    protected void ensureAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return;
        }
        BundleAuthenticationToken token = new BundleAuthenticationToken("openengsb-ports-jms", "");
        authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private SimpleMessageListenerContainer createListenerContainer(String destination, MessageListener listener) {
        SimpleMessageListenerContainer messageListenerContainer = factory.createMessageListenerContainer();
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

    public void setFactory(JMSTemplateFactory factory) {
        this.factory = factory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
