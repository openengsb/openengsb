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

package org.openengsb.itests.remoteclient;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

final class ConnectorMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorMessageListener.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RemoteRequestHandler requestHandler = new RemoteRequestHandler();
    private JmsConfig jmsConfig;

    ConnectorMessageListener(JmsConfig jmsConfig, RemoteRequestHandler requestHandler) {
        this.jmsConfig = jmsConfig;
        this.requestHandler = requestHandler;
    }

    @Override
    public void onMessage(Message message) {
        LOGGER.info("recieved JMS-message");
        TextMessage content = (TextMessage) message;
        String text = getTextFromMessage(content);
        MethodCallRequest request;
        try {
            request = MAPPER.readValue(text, SecureRequest.class).getMessage();
        } catch (IOException e1) {
            throw Throwables.propagate(e1);
        }
        MethodResult result = requestHandler.process(request.getMethodCall());
        try {
            sendResult(request, result);
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    private String getTextFromMessage(TextMessage content) {
        String text;
        try {
            text = content.getText();
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
        return text;
    }

    private void sendResult(MethodCallRequest request, MethodResult result) throws JMSException {
        MethodResultMessage methodResultMessage = new MethodResultMessage(result, request.getCallId());
        SecureResponse secureResponse = SecureResponse.create(methodResultMessage);
        String resultText;
        try {
            resultText = MAPPER.defaultPrettyPrintingWriter().writeValueAsString(secureResponse);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        jmsConfig.sendMessage(request.getCallId(), resultText);
    }
}
