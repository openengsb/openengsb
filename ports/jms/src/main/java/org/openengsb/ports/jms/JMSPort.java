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
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.remote.FilterStorage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSPort implements OutgoingPort {

    private static final String RECEIVE = "receive";

    private JMSTemplateFactory factory;

    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private FilterAction filterChain;

    @Override
    public void send(String destination, MethodCallRequest call) {
        sendMessage(destination, call);
    }

    @Override
    public MethodResultMessage sendSync(String destination, MethodCallRequest call) {
        sendMessage(destination, call);
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.setReceiveTimeout(3000);
        Object receiveAndConvert = createJMSTemplate.receiveAndConvert(call.getCallId());
        if (receiveAndConvert == null) {
            throw new RuntimeException("JMS Receive Timeout reached");
        }
        if (receiveAndConvert instanceof String) {
            return createMethodReturn((String) receiveAndConvert);
        } else {
            throw new IllegalStateException("Message has to be of Type TextMessage and parseable into a String");
        }
    }

    private MethodResultMessage createMethodReturn(String receiveAndConvert) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            MethodResultMessage resultMessage = mapper.readValue(receiveAndConvert, MethodResultMessage.class);
            MethodResult result = resultMessage.getResult();
            Class<?> resultClass = Class.forName(result.getClassName());
            Object arg = mapper.convertValue(result.getArg(), resultClass);
            result.setArg(arg);
            return resultMessage;
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

    private void sendMessage(String destination, MethodCallRequest call) {
        String answer;
        try {
            answer = new ObjectMapper().writeValueAsString(call);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JmsTemplate template = createJMSTemplate(destination);
        template.convertAndSend(answer);
    }

    public void start() {
        simpleMessageListenerContainer = createListenerContainer(RECEIVE, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        String text = textMessage.getText();
                        String result = (String) filterChain.filter(text);
                        Map<String, Object> filterStorage = FilterStorage.getStorage();
                        String callId = (String) filterStorage.get("callId");
                        if (filterStorage.containsKey("answer")) {
                            new JmsTemplate(connectionFactory).convertAndSend(callId, result);
                        }
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        simpleMessageListenerContainer.start();
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


    public void setFilterChain(FilterAction filterChain) {
        this.filterChain = filterChain;
    }
}
