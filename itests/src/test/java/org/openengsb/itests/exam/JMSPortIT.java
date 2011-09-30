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
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.openengsb.labs.paxexam.karaf.options.configs.FeaturesCfg;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class JMSPortIT extends AbstractRemoteTestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSPortIT.class);

    @Configuration
    public Option[] additionalConfiguration() throws Exception {
        return combine(baseConfiguration(), editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-ports-jms"));
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        RuleManager rm = getOsgiService(RuleManager.class);
        addWorkflow("simpleFlow");
        String string = null;
        while (string == null) {
            // TODO OPENENGSB-2097 find a better way than an endless loop
            LOGGER.warn("checking for simpleFlow to be present");
            string = rm.get(new RuleBaseElementId(RuleBaseElementType.Process, "simpleFlow"));
            Thread.sleep(1000);
        }
    }

    @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId =
            OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, "jms-json", 60000);
        assertNotNull(serviceWithId);

    }

    @Test
    public void startSimpleWorkflow_ShouldReturn42() throws Exception {
        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(METHOD_CALL_STRING, "admin", "password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        String result = sendMessage(template, encryptedMessage);

        verifyEncryptedResult(sessionKey, result);
    }

    @Test
    public void startSimpleWorkflowWithFilterMethodCall_ShouldReturn42() throws Exception {
        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(METHOD_CALL_STRING_FILTER, "admin", "password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        String result = sendMessage(template, encryptedMessage);

        verifyEncryptedResult(sessionKey, result);
    }

    @Test
    public void testSendMethodCallWithWrongAuthentication_shouldFail() throws Exception {
        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(METHOD_CALL_STRING, "admin", "wrong-password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        String result = sendMessage(template, encryptedMessage);

        assertThat(result, containsString("Exception"));
        assertThat(result, not(containsString("The answer to life the universe and everything")));
    }

    @Test
    public void recordAuditInCoreService_ShouldReturnVoid() throws Exception {
        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(VOID_CALL_STRING, "admin", "password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        String result = sendMessage(template, encryptedMessage);
        String decryptedResult = decryptResult(sessionKey, result);

        assertThat(decryptedResult, containsString("\"type\":\"Void\""));
        assertThat(decryptedResult, not(containsString("Exception")));
    }

    private String sendMessage(JmsTemplate template, String encryptedMessage) {
        template.convertAndSend("receive", encryptedMessage);
        String result = (String) template.receiveAndConvert("12345");
        return result;
    }

    private JmsTemplate prepareActiveMqConnection() throws IOException {
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        return template;
    }

}
