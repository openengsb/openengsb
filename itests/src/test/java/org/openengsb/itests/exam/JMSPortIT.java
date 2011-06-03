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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.io.InputStream;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.springframework.jms.core.JmsTemplate;

@RunWith(JUnit4TestRunner.class)
public class JMSPortIT extends AbstractExamTestHelper {

    private static final String METHOD_CALL_STRING = ""
            + "{"
            + "    \"classes\": ["
            + "        \"java.lang.String\","
            + "        \"org.openengsb.core.api.workflow.model.ProcessBag\""
            + "    ],"
            + "    \"methodName\": \"executeWorkflow\","
            + "    \"metaData\": {"
            + "        \"serviceId\": \"workflowService\","
            + "        \"contextId\": \"foo\""
            + "    },"
            + "    \"args\": ["
            + "        \"simpleFlow\","
            + "        {"
            + "        }"
            + "    ]"
            + "}";

    private static final String METHOD_CALL_STRING_FILTER = ""
            + "{"
            + "    \"classes\": ["
            + "        \"java.lang.String\","
            + "        \"org.openengsb.core.api.workflow.model.ProcessBag\""
            + "    ],"
            + "    \"methodName\": \"executeWorkflow\","
            + "    \"metaData\": {"
            + "        \"serviceFilter\": \"(objectClass=org.openengsb.core.api.workflow.WorkflowService)\","
            + "        \"contextId\": \"foo\""
            + "    },"
            + "    \"args\": ["
            + "        \"simpleFlow\","
            + "        {"
            + "        }"
            + "    ]"
            + "}";

    private RuleManager ruleManager;

    @Before
    public void setUp() throws Exception {
        ruleManager = getOsgiService(RuleManager.class);
    }

    @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId =
            OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, "jms-json", 60000);
        assertNotNull(serviceWithId);
    }

    @Test
    public void startSimpleWorkflow_ShouldReturn42() throws Exception {
        addWorkflow("simpleFlow");
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        String request = ""
                + "{"
                + "  \"callId\":\"12345\","
                + "  \"answer\":true,"
                + "  \"methodCall\":" + METHOD_CALL_STRING
                + "}";
        template.convertAndSend("receive", request);
        String result = (String) template.receiveAndConvert("12345");
        assertThat(result, containsString("The answer to life the universe and everything"));
    }

    @Test
    public void startSimpleWorkflowWithFilterMethodCall_ShouldReturn42() throws Exception {
        addWorkflow("simpleFlow");
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        String request = ""
                + "{"
                + "  \"callId\":\"12345\","
                + "  \"answer\":true,"
                + "  \"methodCall\":" + METHOD_CALL_STRING_FILTER
                + "}";
        template.convertAndSend("receive", request);
        String result = (String) template.receiveAndConvert("12345");
        assertThat(result, containsString("The answer to life the universe and everything"));
    }

    private void addWorkflow(String workflow) throws IOException, RuleBaseException {
        if (ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, workflow)) == null) {
            InputStream is =
                getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + workflow + ".rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, workflow);
            ruleManager.add(id, testWorkflow);
            IOUtils.closeQuietly(is);
        }
    }
}
