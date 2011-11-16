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
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo;
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

    private static final String registerMessage =
        ""
                + "{\n"
                + "  \"authenticationData\" : {\n"
                + "    \"className\" : \"org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo\",\n"
                + "    \"data\" : {\n"
                + "      \"username\" : \"admin\",\n"
                + "      \"password\" : \"password\"\n"
                + "    },\n"
                + "    \"binaryData\" : {\n"
                + "    }\n"
                + "  },\n"
                + "  \"message\" : {\n"
                + "    \"methodCall\" : {\n"
                + "      \"classes\" : [ \"org.openengsb.core.api.model.ConnectorId\", \"org.openengsb.core.api.model.ConnectorDescription\" ],\n"
                + "      \"methodName\" : \"create\",\n"
                + "      \"args\" : [ {\n"
                + "        \"domainType\" : \"example\",\n"
                + "        \"connectorType\" : \"external-connector-proxy\",\n"
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
                + "      },\n"
                + "      \"realClassImplementation\" : [ \"org.openengsb.core.api.model.ConnectorId\", \"org.openengsb.core.api.model.ConnectorDescription\" ]\n"
                + "    },\n"
                + "    \"callId\" : \"876e6ba5-716b-4e98-953f-ba51235f7a0e\",\n"
                + "    \"answer\" : false,\n"
                + "    \"destination\" : null\n"
                + "  },\n"
                + "  \"timestamp\" : 1321403370228\n"
                + "}\n";

    private static final String unregisterMessage = ""
                + "{\n"
                + "  \"authenticationData\" : {\n"
                + "    \"className\" : \"org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo\",\n"
                + "    \"data\" : {\n"
                + "      \"username\" : \"admin\",\n"
                + "      \"password\" : \"password\"\n"
                + "    },\n"
                + "    \"binaryData\" : {\n"
                + "    }\n"
                + "  },\n"
                + "   \"message\":{\n"
                + "      \"methodCall\":{\n"
                + "         \"classes\":[\n"
                + "            \"org.openengsb.core.api.model.ConnectorId\"\n"
                + "         ],\n"
                + "         \"methodName\":\"delete\",\n"
                + "         \"args\":[\n"
                + "            {\n"
                + "               \"domainType\":\"example\",\n"
                + "               \"connectorType\":\"external-connector-proxy\",\n"
                + "               \"instanceId\":\"example-remote\"\n"
                + "            }\n"
                + "         ],\n"
                + "         \"metaData\":{\n"
                + "            \"serviceId\":\"connectorManager\"\n"
                + "         },\n"
                + "         \"realClassImplementation\":[\n"
                + "            \"org.openengsb.core.api.model.ConnectorId\"\n"
                + "         ]\n"
                + "      },\n"
                + "      \"callId\":\"62589499-58ba-41b9-9594-c9f34883dff1\",\n"
                + "      \"answer\":false,\n"
                + "      \"destination\":null\n"
                + "   },\n"
                + "   \"timestamp\":1321399068682\n"
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
        jmsConfig.sendMessage("receive", registerMessage);
    }

    public void stop() throws JMSException {
        jmsConfig.sendMessage("receive", unregisterMessage);
        jmsConfig.stop();
    }

    public Map<MethodCall, MethodResult> getInvocationHistory() {
        return requestHandler.getInvocationHistory();
    }

    public SecureSampleConnector() {
    }

    public static void createRegisterMessage(String[] args) throws JsonGenerationException,
        JsonMappingException, IOException {
        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new HashMap<String, Object>();

        attributes.put("portId", "jms-json");
        attributes.put("destination", "tcp://127.0.0.1:6549?example-remote");
        attributes.put("serviceId", "example-remote");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);
        ConnectorId connectorId = new ConnectorId("example", "external-connector-proxy", "example-remote");

        MethodCall methodCall = new MethodCall("create", new Object[]{ connectorId, connectorDescription });
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", "connectorManager");
        methodCall.setMetaData(metaData);
        MethodCallRequest methodCallRequest = new MethodCallRequest(methodCall, false);
        BeanDescription auth = BeanDescription.fromObject(new UsernamePasswordAuthenticationInfo("admin", "password"));
        SecureRequest create = SecureRequest.create(methodCallRequest, auth);
        ObjectMapper mapper = new ObjectMapper();
        String writeValueAsString = mapper.defaultPrettyPrintingWriter().writeValueAsString(create);
        System.out.println(writeValueAsString);
    }

    public static void createUnregister(String[] args) throws JsonGenerationException,
        JsonMappingException, IOException {

        ConnectorId connectorId = new ConnectorId("example", "external-connector-proxy", "example-remote");

        MethodCall methodCall = new MethodCall("delete", new Object[]{ connectorId });
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", "connectorManager");
        methodCall.setMetaData(metaData);
        MethodCallRequest methodCallRequest = new MethodCallRequest(methodCall, false);
        SecureRequest create = SecureRequest.create(methodCallRequest, null);
        String writeValueAsString = new ObjectMapper().writeValueAsString(create);
        System.out.println(writeValueAsString);
    }
}
