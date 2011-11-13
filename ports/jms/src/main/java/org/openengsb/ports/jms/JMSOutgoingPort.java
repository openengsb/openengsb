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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

public class JMSOutgoingPort extends AbstractFilterAction<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSOutgoingPort.class);

    private JMSTemplateFactory factory;
    private int timeout;

    public JMSOutgoingPort() {
        super(String.class, String.class);
    }

    @Override
    protected String doFilter(String input, Map<String, Object> metaData) {
        String destination = (String) metaData.get("destination");
        String callId = (String) metaData.get("callId");
        LOGGER.info("sending message with callId {} to destination {}", callId, destination);
        sendMessage(destination, input);

        if (ObjectUtils.notEqual(metaData.get("answer"), true)) {
            LOGGER.debug("no answer expected, just returning null");
            return null;
        }
        LOGGER.info("waiting {}ms for response on call with id {}", timeout, callId);
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.setReceiveTimeout(timeout);
        Object receiveAndConvert = createJMSTemplate.receiveAndConvert(callId);
        if (receiveAndConvert == null) {
            throw new RuntimeException("JMS Receive Timeout reached");
        }
        LOGGER.info("response for call with id {} received", callId);
        return (String) receiveAndConvert;
    }

    private JmsTemplate createJMSTemplate(String destination) {
        return factory.createJMSTemplate(DestinationUrl.createDestinationUrl(destination));
    }

    private void sendMessage(String destination, String message) {
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.convertAndSend(message);
    }

    public void setFactory(JMSTemplateFactory factory) {
        this.factory = factory;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
