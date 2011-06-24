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
import org.openengsb.core.security.BundleAuthenticationToken;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JMSIncomingPort {

    private static final String RECEIVE = "receive";

    private JMSTemplateFactory factory;

    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private FilterChain filterChain;

    private AuthenticationManager authenticationManager;

    public void start() {
        simpleMessageListenerContainer = createListenerContainer(RECEIVE, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        ensureAuthentication();
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

    /**
     * FIXME [OPENENGSB-1226] as soon as authentication over JMS is properly implemented this hack needs to be removed
     * as it grants universal access.
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

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}

