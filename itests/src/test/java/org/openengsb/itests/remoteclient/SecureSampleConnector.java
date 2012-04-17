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
import java.util.Map;

import javax.jms.JMSException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.itests.exam.MessageFormatIT;
import org.openengsb.core.api.security.model.SecureRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Setup to run this app:
 *
 * + Start OpenEngSB
 *
 * + install the jms-feature: features:install openengsb-ports-jms
 *
 * + copy example+external-connector-proxy+example-remote.connector to the openengsb/config-directory
 *
 * + copy openengsb/etc/keys/public.key.data to src/main/resources
 */
public final class SecureSampleConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureSampleConnector.class);
    private static final String URL = "failover:(tcp://localhost:6549)?timeout=60000";
    private JmsConfig jmsConfig;

    private RemoteRequestHandler requestHandler;

    public void start() throws Exception {
        jmsConfig = new JmsConfig(URL);
        jmsConfig.init();
        requestHandler = new RemoteRequestHandler();
        jmsConfig.createConsumerForQueue("example-remote", new ConnectorMessageListener(jmsConfig, requestHandler));
        jmsConfig.sendMessage("receive", MessageFormatIT.CONNECTOR_REGISTRATION_MESSAGE);
    }

    public void stop() throws JMSException {
        jmsConfig.sendMessage("receive", MessageFormatIT.CONNECTOR_UNREGISTER_MESSAGE);
        jmsConfig.stop();
    }

    public Map<MethodCall, MethodResult> getInvocationHistory() {
        return requestHandler.getInvocationHistory();
    }

    public SecureSampleConnector() {
    }

    public static void main(String[] args) throws JsonGenerationException,
        JsonMappingException, IOException {

    }
}
