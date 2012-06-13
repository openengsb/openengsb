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

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
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
            + "  \"methodCall\" : {\n"
            + "    \"methodName\" : \"createWithId\",\n"
            + "    \"args\" : [ \"example-remote\", {\n"
            + "      \"domainType\" : \"example\",\n"
            + "      \"connectorType\" : \"external-connector-proxy\",\n"
            + "      \"attributes\" : {\n"
            + "        \"serviceId\" : \"example-remote\",\n"
            + "        \"portId\" : \"jms-json\",\n"
            + "        \"destination\" : \"tcp://127.0.0.1:6549?example-remote\"\n"
            + "      },\n"
            + "      \"properties\" : {\n"
            + "      }\n"
            + "    } ],\n"
            + "    \"metaData\" : {\n"
            + "      \"serviceId\" : \"connectorManager\"\n"
            + "    },\n"
            + "    \"classes\" : [ \"java.lang.String\", \"org.openengsb.core.api.model.ConnectorDescription\" ],\n"
            + "    \"realClassImplementation\" : [ \"java.lang.String\", "
            + "\"org.openengsb.core.api.model.ConnectorDescription\" ]\n"
            + "  },\n"
            + "  \"callId\" : \"1d075f48-53ee-427a-ae8a-8e9d5b6db229\",\n"
            + "  \"answer\" : false,\n"
            + "  \"destination\" : null,\n"
            + "  \"timestamp\" : 1336060640851\n"
            + "}\n";

    private static final String UNREGISTER_MESSAGE = ""
            + "{"
            + "  \"methodCall\" : {"
            + "    \"methodName\" : \"delete\","
            + "    \"args\" : [ \"example-remote\" ],"
            + "    \"metaData\" : {"
            + "      \"serviceId\" : \"connectorManager\""
            + "    },"
            + "    \"classes\" : [ \"java.lang.String\" ],"
            + "    \"realClassImplementation\" : [ \"java.lang.String\" ]"
            + "  },"
            + "  \"callId\" : \"62259d96-bcae-4450-bded-850a7f06f2ac\","
            + "  \"answer\" : false,"
            + "  \"destination\" : null,"
            + "  \"timestamp\" : 1336060561647,"
            + "  \"principal\" : \"admin\","
            + "  \"credentials\" : {"
            + "    \"className\" : \"org.openengsb.connector.usernamepassword.Password\","
            + "    \"data\" : {"
            + "      \"value\" : \"password\""
            + "    },"
            + "    \"binaryData\" : {"
            + "    }"
            + "  }"
            + "}";

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

    public static void createRegisterMessage(String[] args) throws IOException {
        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new HashMap<String, Object>();

        attributes.put("portId", "jms-json");
        attributes.put("destination", "tcp://127.0.0.1:6549?example-remote");
        attributes.put("serviceId", "example-remote");
        ConnectorDescription connectorDescription = new ConnectorDescription("example", "external-connector-proxy",
            attributes, properties);

        String connectorId = "example-remote";
        MethodCall methodCall = new MethodCall("createWithId", new Object[]{ connectorId, connectorDescription });
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", "connectorManager");
        methodCall.setMetaData(metaData);
        MethodCallMessage methodCallRequest = new MethodCallMessage(methodCall, false);
        BeanDescription auth = BeanDescription.fromObject(new Password("password"));
        methodCallRequest.setPrincipal("admin");
        methodCallRequest.setCredentials(auth);

        ObjectMapper mapper = new ObjectMapper();
        String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(methodCallRequest);
        System.out.println(writeValueAsString);
    }

    public static void main(String[] args) throws IOException {
        String connectorId = "example-remote";
        MethodCall methodCall = new MethodCall("delete", new Object[]{ connectorId });
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", "connectorManager");
        methodCall.setMetaData(metaData);
        MethodCallMessage methodCallRequest = new MethodCallMessage(methodCall, false);
        BeanDescription auth = BeanDescription.fromObject(new Password("password"));
        methodCallRequest.setPrincipal("admin");
        methodCallRequest.setCredentials(auth);
        ObjectMapper mapper = new ObjectMapper();
        String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(methodCallRequest);
        System.out.println(writeValueAsString);
    }
}
