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
package org.openengsb.itests.exam;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.api.remote.GenericObjectSerializer;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.workflow.model.ProcessBag;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@RunWith(JUnit4TestRunner.class)
// @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class MessageFormatIT extends AbstractPreConfiguredExamTestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFormatIT.class);

    public static final String METHOD_CALL_STRING = ""
            + "{"
            + "  \"methodName\" : \"executeWorkflow\","
            + "  \"args\" : [ \"simpleFlow\", {"
            + "    \"@type\" : \"org.openengsb.core.api.workflow.model.ProcessBag\","
            + "    \"processId\" : null,"
            + "    \"context\" : null,"
            + "    \"user\" : null,"
            + "    \"properties\" : {"
            + "    }"
            + "  } ],"
            + "  \"metaData\" : {"
            + "    \"serviceId\" : \"workflowService\","
            + "    \"contextId\" : \"foo\""
            + "  }"
            + "}";

    public static final String VOID_CALL_STRING = ""
            + "{"
            + "  \"methodName\" : \"audit\","
            + "  \"args\" : [ {"
            + "    \"@type\" : \"Event\","
            + "    \"name\" : \"testMessage\","
            + "    \"processId\" : null,"
            + "    \"origin\" : null"
            + "  } ],"
            + "  \"metaData\" : {"
            + "    \"serviceId\" : \"auditing+memoryauditing+auditing-root\","
            + "    \"contextId\" : \"foo\""
            + "  }"
            + "}";

    public static final String METHOD_CALL_STRING_FILTER = ""
            + "{"
            + "  \"methodName\" : \"executeWorkflow\","
            + "  \"args\" : [ \"simpleFlow\", {"
            + "    \"@type\" : \"org.openengsb.core.api.workflow.model.ProcessBag\","
            + "    \"processId\" : null,"
            + "    \"context\" : null,"
            + "    \"user\" : null,"
            + "    \"properties\" : {"
            + "    }"
            + "  } ],"
            + "  \"metaData\" : {"
            + "    \"serviceFilter\" : \"(objectClass=org.openengsb.core.api.workflow.WorkflowService)\","
            + "    \"contextId\" : \"foo\""
            + "  }"
            + "}";

    public static final String METHOD_CALL_WITH_MODEL_PARAMETER = ""
            + "{"
            + "  \"methodName\" : \"doSomethingWithModel\","
            + "  \"args\" : [ {"
            + "    \"@type\" : \"ModelWrapper\","
            + "    \"modelClass\" : \"org.openengsb.domain.example.model.ExampleRequestModel\","
            + "    \"entries\" : [ {"
            + "      \"key\" : \"id\","
            + "      \"value\" : 10,"
            + "      \"type\" : \"java.lang.Integer\""
            + "    }, {"
            + "      \"key\" : \"name\","
            + "      \"value\" : \"test\","
            + "      \"type\" : \"java.lang.String\""
            + "    } ]"
            + "  } ],"
            + "  \"metaData\" : {"
            + "    \"serviceId\" : \"test\""
            + "  }"
            + "}";

    public static final String CONNECTOR_REGISTRATION_MESSAGE = ""
            + "{"
            + "  \"message\" : {"
            + "    \"methodCall\" : {"
            + "      \"methodName\" : \"create\","
            + "      \"args\" : [ {"
            + "        \"@type\" : \"ConnectorDefinition\","
            + "        \"domainId\" : \"example\","
            + "        \"connectorId\" : \"external-connector-proxy\","
            + "        \"instanceId\" : \"example-remote\""
            + "      }, {"
            + "        \"@type\" : \"org.openengsb.core.api.model.ConnectorDescription\","
            + "        \"attributes\" : {"
            + "          \"portId\" : \"jms-json\","
            + "          \"destination\" : \"tcp://127.0.0.1:6549?example-remote\","
            + "          \"serviceId\" : \"example-remote\""
            + "        },"
            + "        \"properties\" : {"
            + "        }"
            + "      } ],"
            + "      \"metaData\" : {"
            + "        \"serviceId\" : \"connectorManager\""
            + "      }"
            + "    },"
            + "    \"callId\" : \"41a28f22-fa00-460d-beab-ebb5e3ee17a7\","
            + "    \"answer\" : false,"
            + "    \"destination\" : null"
            + "  },"
            + "  \"timestamp\" : 1330434818357,"
            + "  \"principal\" : \"admin\","
            + "  \"credentials\" : {"
            + "    \"@type\" : \"Password\","
            + "    \"value\" : \"password\""
            + "  }"
            + "}";

    public static final String CONNECTOR_UNREGISTER_MESSAGE = ""
            + "{"
            + "  \"message\" : {"
            + "    \"methodCall\" : {"
            + "      \"methodName\" : \"delete\","
            + "      \"args\" : [ {"
            + "        \"@type\" : \"ConnectorDefinition\","
            + "        \"domainId\" : \"example\","
            + "        \"connectorId\" : \"external-connector-proxy\","
            + "        \"instanceId\" : \"example-remote\""
            + "      } ],"
            + "      \"metaData\" : {"
            + "        \"serviceId\" : \"connectorManager\""
            + "      }"
            + "    },"
            + "    \"callId\" : \"26fadcc6-d6ec-48f6-bb5f-e57957ab258a\","
            + "    \"answer\" : false,"
            + "    \"destination\" : null"
            + "  },"
            + "  \"timestamp\" : 1330435116506,"
            + "  \"principal\" : \"admin\","
            + "  \"credentials\" : {"
            + "    \"@type\" : \"Password\","
            + "    \"value\" : \"password\""
            + "  }"
            + "}";

    private GenericObjectSerializer objectSerializer;

    @Before
    public void setUp() throws Exception {
        objectSerializer = getOsgiService(GenericObjectSerializer.class);
    }

    private void assertEqualJsonTree(String expected, String actual) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(objectMapper.readTree(expected), objectMapper.readTree(actual));
    }

    @Test
    public void testCreateMethodCallString() throws Exception {
        MethodCall methodCall = new MethodCall("executeWorkflow", new Object[]{ "simpleFlow", new ProcessBag() });
        methodCall.setMetaData(ImmutableMap.of(
            "serviceId", "workflowService",
            "contextId", "foo"));
        String methodCallString = objectSerializer.serializeToString(methodCall);
        LOGGER.info(methodCallString);
        assertEqualJsonTree(METHOD_CALL_STRING, methodCallString);
    }

    @Test
    public void testCreateMethodCallStringFilter() throws Exception {
        MethodCall methodCall = new MethodCall("executeWorkflow", new Object[]{ "simpleFlow", new ProcessBag() });
        methodCall.setMetaData(ImmutableMap.of(
            "serviceFilter", "(objectClass=org.openengsb.core.api.workflow.WorkflowService)",
            "contextId", "foo"));
        String methodCallString = objectSerializer.serializeToString(methodCall);
        LOGGER.info(methodCallString);
        assertEqualJsonTree(METHOD_CALL_STRING_FILTER, methodCallString);
    }

    @Test
    public void testCreateVoidCallString() throws Exception {
        MethodCall methodCall = new MethodCall("audit", new Object[]{ new Event("testMessage") });
        methodCall.setMetaData(ImmutableMap.of(
            "serviceId", "auditing+memoryauditing+auditing-root",
            "contextId", "foo"));
        String methodCallString = objectSerializer.serializeToString(methodCall);
        LOGGER.info(methodCallString);
        assertEqualJsonTree(VOID_CALL_STRING, methodCallString);
    }

    // @Ignore("callid and timestamp always change")
    @Test
    public void createRegisterMessage() throws Exception {
        Map<String, String> attributes = ImmutableMap.of(
            "portId", "jms-json",
            "destination", "tcp://127.0.0.1:6549?example-remote",
            "serviceId", "example-remote");
        Map<String, Object> properties = Maps.newHashMap();

        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);
        ConnectorDefinition connectorId =
            new ConnectorDefinition("example", "external-connector-proxy", "example-remote");

        MethodCall methodCall = new MethodCall("create", new Object[]{ connectorId, connectorDescription });

        methodCall.setMetaData(ImmutableMap.of("serviceId", "connectorManager"));

        MethodCallRequest methodCallRequest = new MethodCallRequest(methodCall, false);
        Credentials auth = new Password("password");

        SecureRequest secureRequest = SecureRequest.create(methodCallRequest, "admin", auth);
        String requestString = objectSerializer.serializeToString(secureRequest);

        LOGGER.info(requestString);
    }

    // @Ignore("callid and timestamp always change")
    @Test
    public void createUnregisterMessage() throws Exception {
        ConnectorDefinition connectorId =
            new ConnectorDefinition("example", "external-connector-proxy", "example-remote");
        MethodCall methodCall = new MethodCall("delete", new Object[]{ connectorId });
        methodCall.setMetaData(ImmutableMap.of(
            "serviceId", "connectorManager"));
        MethodCallRequest methodCallRequest = new MethodCallRequest(methodCall, false);
        Credentials auth = new Password("password");
        SecureRequest request = SecureRequest.create(methodCallRequest, "admin", auth);
        String requestString = objectSerializer.serializeToString(request);
        LOGGER.info(requestString);
    }

    @Test
    public void createCallWithModel() throws Exception {
        ExampleRequestModel model = ModelUtils.createEmptyModelObject(ExampleRequestModel.class);
        model.setId(10);
        model.setName("test");

        OpenEngSBModelWrapper wrapper = new OpenEngSBModelWrapper();
        wrapper.setEntries(model.getOpenEngSBModelEntries());
        wrapper.setModelClass(ExampleRequestModel.class.getName());

        MethodCall methodCall = new MethodCall("doSomethingWithModel", new Object[]{ wrapper });
        methodCall.setMetaData(ImmutableMap.of("serviceId", "test"));

        String requestString = objectSerializer.serializeToString(methodCall);
        LOGGER.info(requestString);
        assertEqualJsonTree(METHOD_CALL_WITH_MODEL_PARAMETER, requestString);
    }

}
