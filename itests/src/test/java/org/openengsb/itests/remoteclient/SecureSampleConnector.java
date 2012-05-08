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
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
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

    private static final String REGISTER_MESSAGE = ""
            + "{\n"
            + "  \"principal\" : \"admin\",\n"
            + "  \"credentials\" : {\n"
            + "    \"className\" : \"org.openengsb.connector.usernamepassword.Password\",\n"
            + "    \"data\" : {\n"
            + "      \"value\" : \"password\"\n"
            + "    },\n"
            + "    \"binaryData\" : {\n"
            + "    }\n"
            + "  },\n"
            + "  \"message\" : {\n"
            + "    \"methodCall\" : {\n"
            + "      \"classes\" : [ \"org.openengsb.core.api.model.ConnectorDefinition\", "
            + "           \"org.openengsb.core.api.model.ConnectorDescription\" ],\n"
            + "      \"methodName\" : \"create\",\n"
            + "      \"realClassImplementation\" : [ \"org.openengsb.core.api.model.ConnectorDefinition\", "
            + "           \"org.openengsb.core.api.model.ConnectorDescription\" ],\n"
            + "      \"args\" : [ {\n"
            + "        \"domainId\" : \"example\",\n"
            + "        \"connectorId\" : \"external-connector-proxy\",\n"
            + "        \"instanceId\" : \"example-remote\"\n"
            + "      }, {\n"
            + "        \"properties\" : {\n"
            + "        },\n"
            + "        \"attributes\" : {\n"
            + "          \"serviceId\" : \"example-remote\",\n"
            + "          \"portId\" : \"jms-json\",\n"
            + "          \"destination\" : \"tcp://127.0.0.1:6549?example-remote\"\n"
            + "        }\n"
            + "      } ],\n"
            + "      \"metaData\" : {\n"
            + "        \"serviceId\" : \"connectorManager\"\n"
            + "      }\n"
            + "    },\n"
            + "    \"callId\" : \"1d075f48-53ee-427a-ae8a-8e9d5b6db229\",\n"
            + "    \"answer\" : false,\n"
            + "    \"destination\" : null\n"
            + "  },\n"
            + "  \"timestamp\" : 1322173854513\n"
            + "}\n";

    private static final String UNREGISTER_MESSAGE = ""
            + "{\n"
            + "  \"principal\" : \"admin\",\n"
            + "  \"credentials\" : {\n"
            + "    \"className\" : \"org.openengsb.connector.usernamepassword.Password\",\n"
            + "    \"data\" : {\n"
            + "      \"value\" : \"password\"\n"
            + "    },\n"
            + "    \"binaryData\" : {\n"
            + "    }\n"
            + "  },\n"
            + "  \"message\" : {\n"
            + "    \"methodCall\" : {\n"
            + "      \"classes\" : [ \"org.openengsb.core.api.model.ConnectorDefinition\" ],\n"
            + "      \"methodName\" : \"delete\",\n"
            + "      \"realClassImplementation\" : [ \"org.openengsb.core.api.model.ConnectorDefinition\" ],\n"
            + "      \"args\" : [ {\n"
            + "        \"domainId\" : \"example\",\n"
            + "        \"connectorId\" : \"external-connector-proxy\",\n"
            + "        \"instanceId\" : \"example-remote\"\n"
            + "      } ],\n"
            + "      \"metaData\" : {\n"
            + "        \"serviceId\" : \"connectorManager\"\n"
            + "      }\n"
            + "    },\n"
            + "    \"callId\" : \"963718b8-07bf-4478-af12-b28bd47248b1\",\n"
            + "    \"answer\" : false,\n"
            + "    \"destination\" : null\n"
            + "  },\n"
            + "  \"timestamp\" : 1322174540600\n"
            + "}\n";

    static final Logger LOGGER = LoggerFactory.getLogger(SecureSampleConnector.class);
    private static final String URL = "failover:(tcp://localhost:6549)?timeout=60000";
    private JmsConfig jmsConfig;

    private RemoteRequestHandler requestHandler;

    public void start() throws Exception {
        jmsConfig = new JmsConfig(URL);
        jmsConfig.init();
        requestHandler = new RemoteRequestHandler();
        jmsConfig.createConsumerForQueue("example-remote", new ConnectorMessageListener(jmsConfig, requestHandler));
        jmsConfig.sendMessage("receive", REGISTER_MESSAGE);
    }

    public void stop() throws JMSException {
        jmsConfig.sendMessage("receive", UNREGISTER_MESSAGE);
        jmsConfig.stop();
    }

    public Map<MethodCall, MethodResult> getInvocationHistory() {
        return requestHandler.getInvocationHistory();
    }

    public static void createRegisterMessage(String[] args) throws JsonGenerationException,
        JsonMappingException, IOException {
        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new HashMap<String, Object>();

        attributes.put("portId", "jms-json");
        attributes.put("destination", "tcp://127.0.0.1:6549?example-remote");
        attributes.put("serviceId", "example-remote");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);
        ConnectorDefinition connectorId =
            new ConnectorDefinition("example", "external-connector-proxy", "example-remote");

        MethodCall methodCall = new MethodCall("create", new Object[]{ connectorId, connectorDescription });
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", "connectorManager");
        methodCall.setMetaData(metaData);
        MethodCallRequest methodCallRequest = new MethodCallRequest(methodCall, false);
        BeanDescription auth = BeanDescription.fromObject(new Password("password"));
        methodCallRequest.setPrincipal("admin");
        methodCallRequest.setCredentials(auth);

        ObjectMapper mapper = new ObjectMapper();
        String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(methodCallRequest);
        System.out.println(writeValueAsString);
    }

    public static void main(String[] args) throws JsonGenerationException,
        JsonMappingException, IOException {

        ConnectorDefinition connectorId =
            new ConnectorDefinition("example", "external-connector-proxy", "example-remote");

        MethodCall methodCall = new MethodCall("delete", new Object[]{ connectorId });
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", "connectorManager");
        methodCall.setMetaData(metaData);
        MethodCallRequest methodCallRequest = new MethodCallRequest(methodCall, false);
        BeanDescription auth = BeanDescription.fromObject(new Password("password"));
        methodCallRequest.setPrincipal("admin");
        methodCallRequest.setCredentials(auth);
        ObjectMapper mapper = new ObjectMapper();
        String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(methodCallRequest);
        System.out.println(writeValueAsString);
    }
}
