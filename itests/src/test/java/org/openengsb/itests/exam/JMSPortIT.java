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
import java.util.Hashtable;

import javax.crypto.SecretKey;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.remoteclient.SecureSampleConnector;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.openengsb.labs.paxexam.karaf.options.configs.FeaturesCfg;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class JMSPortIT extends AbstractRemoteTestHelper {

    @Configuration
    public Option[] additionalConfiguration() throws Exception {
        return combine(baseConfiguration(), editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-ports-jms"));
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

    @Test
    public void testStartAndStopRemoteConnector_shouldRegisterAndUnregisterProxy() throws Exception {
        // make sure security-stuff is off
        System.setProperty("org.openengsb.jms.noencrypt", "true");
        System.setProperty("org.openengsb.security.noverify", "true");

        // make sure jms is up and running
        OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, "jms-json", 60000);

        SecureSampleConnector remoteConnector = new SecureSampleConnector();
        remoteConnector.start();
        ExampleDomain osgiService =
            getOsgiService(ExampleDomain.class, "(id=example+external-connector-proxy+example-remote)", 31000);

        assertThat(getBundleContext().getServiceReferences(ExampleDomain.class.getName(),
            "(id=example+external-connector-proxy+example-remote)"), not(nullValue()));
        assertThat(osgiService, not(nullValue()));

        remoteConnector.getInvocationHistory().clear();
        osgiService.doSomething("test");
        assertThat(remoteConnector.getInvocationHistory().isEmpty(), is(false));

        remoteConnector.stop();
        Thread.sleep(5000);
        assertThat(getBundleContext().getServiceReferences(ExampleDomain.class.getName(),
            "(id=example+external-connector-proxy+example-remote)"), nullValue());
    }

    @Test
    public void testSendMethodWithModelAsParamter_shouldWork() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("id", "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.root", new String[]{ "foo" });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(METHOD_CALL_WITH_MODEL_PARAMETER, "admin", "password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        String result = sendMessage(template, encryptedMessage);
        String decryptedResult = decryptResult(sessionKey, result);
        
        assertThat(decryptedResult.contains("successful"), is(true));
    }

    private String sendMessage(JmsTemplate template, String encryptedMessage) {
        template.convertAndSend("receive", encryptedMessage);
        String result = (String) template.receiveAndConvert("12345");
        return result;
    }

    private JmsTemplate prepareActiveMqConnection() throws IOException {
        addWorkflow("simpleFlow");
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        return template;
    }

    public class DummyService extends AbstractOpenEngSBService implements ExampleDomain {

        public DummyService(String instanceId) {
            super(instanceId);
        }

        @Override
        public String doSomething(ExampleEnum exampleEnum) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public String doSomething(String message) {
            return "success : " + message;
        }

        @Override
        public String doSomethingWithLogEvent(LogEvent event) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public AliveState getAliveState() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public ExampleResponseModel doSomething(ExampleRequestModel model) {
            ExampleResponseModel response = ModelUtils.createEmptyModelObject(ExampleResponseModel.class);
            response.setResult("successful");
            return response;
        }

    }

}
