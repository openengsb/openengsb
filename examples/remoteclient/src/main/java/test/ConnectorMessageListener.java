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

package test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;

import com.google.common.base.Throwables;

final class ConnectorMessageListener implements MessageListener {
    private MessageHandler handler;
    private RequestHandler requestHander = new RequestHandler();
    private JmsConfig jmsConfig;

    ConnectorMessageListener(JmsConfig jmsConfig) {
        handler = new MessageHandler();
        this.jmsConfig = jmsConfig;
    }

    @Override
    public void onMessage(Message message) {
        SecureSampleConnector.LOGGER.info("recieved JMS-message");
        TextMessage content = (TextMessage) message;
        String text = getTextFromMessage(content);
        MethodCallMessage request = handler.unmarshal(text);
        MethodResult result = requestHander.process(request.getMethodCall());
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

    private void sendResult(MethodCallMessage request, MethodResult result) throws JMSException {
        MethodResultMessage methodResultMessage = new MethodResultMessage(result, request.getCallId());
        String resultText = handler.marshal(methodResultMessage);
        jmsConfig.sendMessage(request.getCallId(), resultText);
    }
}
