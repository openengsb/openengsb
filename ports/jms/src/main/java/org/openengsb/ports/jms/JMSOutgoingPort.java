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

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.common.remote.AbstractFilterAction;
import org.springframework.jms.core.JmsTemplate;

public class JMSOutgoingPort extends AbstractFilterAction<String, String> {

    private static final String RECEIVE = "receive";

    private JMSTemplateFactory factory;

    public JMSOutgoingPort() {
        super(String.class, String.class);
    }

    @Override
    protected String doFilter(String input, Map<String, Object> metaData) {
        String destination = (String) metaData.get("destination");
        String callId = (String) metaData.get("callId");
        sendMessage(destination, input);

        if (ObjectUtils.notEqual(metaData.get("answer"), true)) {
            return null;
        }
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.setReceiveTimeout(3000);
        Object receiveAndConvert = createJMSTemplate.receiveAndConvert(callId);
        if (receiveAndConvert == null) {
            throw new RuntimeException("JMS Receive Timeout reached");
        }
        return (String) receiveAndConvert;
    }

    private JmsTemplate createJMSTemplate(String destination) {
        return factory.createJMSTemplate(DestinationUrl.createDestinationUrl(destination));
    }

    private void sendMessage(String destination, String message) {
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.convertAndSend(RECEIVE, message);
    }

    public void setFactory(JMSTemplateFactory factory) {
        this.factory = factory;
    }
}
