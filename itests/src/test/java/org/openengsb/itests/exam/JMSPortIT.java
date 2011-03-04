/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.itests.exam;

import static junit.framework.Assert.assertNotNull;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.springframework.jms.core.JmsTemplate;

@RunWith(JUnit4TestRunner.class)
public class JMSPortIT extends AbstractExamTestHelper {

    // @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId =
            OsgiServiceUtils.getServiceWithId(OutgoingPort.class, "jms-json");
        System.out.println("ServiceID:" + serviceWithId);
        assertNotNull(serviceWithId);
    }

    @Test
    public void startSimpleWorkflow_ShouldReturn42() {
        System.out.println("Starting Integration Test");
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:6549");
        JmsTemplate template = new JmsTemplate(cf);
        // template.setReceiveTimeout(4000);
        String request =
            "{\"callId\":\"12345\",\"answer\":true,\"classes\":[\"java.lang.String\", \"org.openengsb.core.common.workflow.model.ProcessBag\"],"
                    + "\"methodName\":\"executeWorkflow\",\"metaData\":{\"serviceId\":\"workflowService\"},\"args\":[\"simpleFlow\", {}]}";
        System.out.println("Seding request");
        template.convertAndSend("receive", request);
        Object receiveAndConvert = template.receiveAndConvert("12345");
        System.out.println(receiveAndConvert);
    }
}
