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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.openengsb.core.common.remote.FilterChain;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSIncomingPort {

    private static final String RECEIVE = "receive";

    private JMSTemplateFactory factory;

    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private FilterChain<String, String> filterChain;

    public void start() {
        simpleMessageListenerContainer = createListenerContainer(RECEIVE, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        String text = textMessage.getText();
                        HashMap<String, Object> metadata = new HashMap<String, Object>();
                        String result = (String) filterChain.filter(text, metadata);
                        String callId = (String) metadata.get("callId");
                        if (metadata.containsKey("answer")) {
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

    public void setFilterChain(FilterChain<String, String> filterChain) {
        this.filterChain = filterChain;
    }
}
