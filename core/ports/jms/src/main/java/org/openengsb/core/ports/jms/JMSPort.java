/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.ports.jms;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.common.communication.IncomingPort;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.communication.RequestHandler;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JMSPort implements IncomingPort, OutgoingPort {

    private static final String RECEIVE = "receive";

    private JMSTemplateFactory factory;

    private ConnectionFactory connectionFactory;

    private RequestHandler requestHandler;
    private AuthenticationManager authenticationManager;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private final ObjectMapper mapper = new ObjectMapper();

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
            ReturnMapping returnValue = mapper.readValue(receiveAndConvert, ReturnMapping.class);
            if (returnValue.getType() != ReturnType.Void) {
                Class<?> classValue = Class.forName(returnValue.getClassName());
                returnValue.setArg(mapper.convertValue(returnValue.getArg(), classValue));
            }
            return returnValue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private JmsTemplate createJMSTemplate(String destination) {
        return factory.createJMSTemplate(destination);
    }

    private void sendMessage(String destination, MethodCall call) {
        StringWriter result = new StringWriter();
        try {
            new ObjectMapper().writeValue(result, call);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.convertAndSend(RECEIVE, result.toString());
    }

    public void setRequestHandler(RequestHandler handler) {
        this.requestHandler = handler;
    }

    @Override
    public void start() {
        this.simpleMessageListenerContainer = createListenerContainer(RECEIVE, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    ObjectMapper mapper = new ObjectMapper();
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        ensureAuthentication();
                        RequestMapping readValue =
                            mapper.readValue(new StringReader(textMessage.getText()), RequestMapping.class);
                        readValue.resetArgs();
                        MethodReturn handleCall = requestHandler.handleCall(readValue);
                        StringWriter stringWriter = new StringWriter();
                        mapper.writeValue(stringWriter, handleCall);

                        if (readValue.isAnswer()) {
                            new JmsTemplate(connectionFactory).convertAndSend(readValue.getCallId(),
                                stringWriter.toString());
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
     * FIXME [OPENENGSB-1226] as soon as authentication over JMS is properly implemented this hack needs to 
     * be removed as it grants universal access.
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

    @Override
    public void stop() {
        if (this.simpleMessageListenerContainer != null) {
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
