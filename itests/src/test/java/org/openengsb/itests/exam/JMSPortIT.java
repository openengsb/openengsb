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

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.springframework.jms.core.JmsTemplate;

@RunWith(JUnit4TestRunner.class)
public class JMSPortIT extends AbstractRemoteTestHelper {

    @Configuration
    public final Option[] configureJms() {
        return options(scanFeatures(
            maven().groupId("org.openengsb").artifactId("openengsb").type("xml").classifier("features-itests")
                .versionAsInProject(), "openengsb-ports-jms"));
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
        template.convertAndSend("receive", getRequest("12345"));
        String result = (String) template.receiveAndConvert("12345");

        assertThat(result, containsString("The answer to life the universe and everything"));
    }

    @Test
    public void recordAuditInCoreService_ShouldReturnVoid() throws Exception {
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        template.convertAndSend("receive", getAuditingRequest("12345"));
        String result = (String) template.receiveAndConvert("12345");

        System.out.println(result);

        assertThat(result, containsString("\"type\":\"Void\""));
        assertThat(result, not(containsString("Exception")));
    }
}
