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

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.IOException;
import java.util.Hashtable;

import javax.crypto.SecretKey;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.model.ModelWrapper;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.core.util.JsonUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(JUnit4TestRunner.class)
public class JMSPortIT extends AbstractRemoteTestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSPortIT.class);
    private DefaultOsgiUtilsService utilsService;
    private String openwirePort;

    @Configuration
    public Option[] additionalConfiguration() throws Exception {
        return combine(baseConfiguration(),
            editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-ports-jms,openengsb-connector-example"),
            new VMOption("-Dorg.openengsb.jms.noencrypt=false"),
            new VMOption("-Dorg.openengsb.security.noverify=false"));
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        openwirePort = getOpenwirePort();
        additionalJMSSetUp(LOGGER);
        utilsService = getOsgiUtils();
    }

    @Test
    public void testJmsPortPresence_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId = utilsService.getServiceWithId(OutgoingPort.class, "jms-json", 60000);
        assertNotNull(serviceWithId);
    }

    @Test
    public void testStartSimpleWorkflow_ShouldReturn42() throws Exception {
        EncryptedAnswer answer = sendMessage(METHOD_CALL_STRING);
        verifyEncryptedResult(answer.getSessionKey(), answer.getAnswer());
    }

    @Test
    public void testStartSimpleWorkflowWithFilterMethodCall_ShouldReturn42() throws Exception {
        EncryptedAnswer answer = sendMessage(METHOD_CALL_STRING_FILTER);
        verifyEncryptedResult(answer.getSessionKey(), answer.getAnswer());
    }

    @Test
    public void testSendMethodCallWithWrongAuthentication_shouldFail() throws Exception {
        String result = sendMessage(METHOD_CALL_STRING, "admin", "wrong-password").getAnswer();
        assertThat(result, containsString("Exception"));
        assertThat(result, not(containsString("The answer to life the universe and everything")));
    }

    @Test
    public void testRecordAuditInCoreService_shouldReturnVoid() throws Exception {
        EncryptedAnswer answer = sendMessage(VOID_CALL_STRING);
        String decryptedResult = decryptResult(answer.getSessionKey(), answer.getAnswer());
        assertThat(decryptedResult, containsString("\"type\":\"Void\""));
        assertThat(decryptedResult, not(containsString("Exception")));
    }

    @Test
    public void testSendMethodWithModelAsParamter_shouldWork() throws Exception {
        registerDummyService();
        EncryptedAnswer answer = sendMessage(METHOD_CALL_WITH_MODEL_PARAMETER);
        String decryptedResult = decryptResult(answer.getSessionKey(), answer.getAnswer());
        ExampleResponseModel model = extractResponseModelFromMethodResult(decryptedResult);
        assertThat(decryptedResult.contains("successful"), is(true));
        assertThat(model.getResult(), is("successful"));
    }

    @Test
    public void testSendMethodWithModelIncludingTailAsParamter_shouldWork() throws Exception {
        registerDummyService();
        EncryptedAnswer answer = sendMessage(METHOD_CALL_WITH_MODEL_INCLUDING_TAIL_PARAMETER);
        String decryptedResult = decryptResult(answer.getSessionKey(), answer.getAnswer());
        ExampleResponseModel model = extractResponseModelFromMethodResult(decryptedResult);
        assertThat(decryptedResult.contains("successful with tail"), is(true));
        assertThat(model.getResult(), is("successful with tail"));
    }

    private ExampleResponseModel extractResponseModelFromMethodResult(String jsonMessage) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MethodResultMessage methodResult = mapper.readValue(jsonMessage, MethodResultMessage.class);
        JsonUtils.convertResult(methodResult);
        return (ExampleResponseModel) methodResult.getResult().getArg();
    }

    private void registerDummyService() throws Exception {
        if (isOsgiServiceAvailable(ExampleDomain.class, "(service.pid=test)")) {
            // if we get here, the domain is already registered
            return;
        }
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.root", new String[]{ "foo" });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);
    }

    private EncryptedAnswer sendMessage(String methodCall) throws Exception {
        return sendMessage(methodCall, "admin", "password");
    }

    private EncryptedAnswer sendMessage(String methodCall, String username, String password) throws Exception {
        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(methodCall, username, password);
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);
        String answer = sendMessage(template, encryptedMessage);
        return new EncryptedAnswer(answer, sessionKey);
    }

    private String sendMessage(final JmsTemplate template, final String msg) {
        String resultString = template.execute(new SessionCallback<String>() {
            @Override
            public String doInJms(Session session) throws JMSException {
                Queue queue = session.createQueue("receive");
                MessageProducer producer = session.createProducer(queue);
                TemporaryQueue tempQueue = session.createTemporaryQueue();
                MessageConsumer consumer = session.createConsumer(tempQueue);
                TextMessage message = session.createTextMessage(msg);
                message.setJMSReplyTo(tempQueue);
                producer.send(message);
                TextMessage response = (TextMessage) consumer.receive(35000);
                assertThat("server should set the value of the correltion ID to the value of the received message id",
                    response.getJMSCorrelationID(), is(message.getJMSMessageID()));
                JmsUtils.closeMessageProducer(producer);
                JmsUtils.closeMessageConsumer(consumer);
                return response.getText();
            }
        }, true);
        return resultString;
    }

    private JmsTemplate prepareActiveMqConnection() throws IOException {
        String connection = String.format("failover:(tcp://localhost:%s)?timeout=60000", openwirePort);
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(connection);
        JmsTemplate template = new JmsTemplate(cf);
        return template;
    }

    private class EncryptedAnswer {
        private String answer;
        private SecretKey sessionKey;

        public EncryptedAnswer(String answer, SecretKey sessionKey) {
            this.answer = answer;
            this.sessionKey = sessionKey;
        }

        public String getAnswer() {
            return answer;
        }

        public SecretKey getSessionKey() {
            return sessionKey;
        }
    }

    public class DummyService extends AbstractOpenEngSBService implements ExampleDomain {

        public DummyService(String instanceId) {
            super(instanceId);
        }

        @Override
        public String doSomethingWithMessage(String message) {
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
        public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
            ExampleResponseModel response = new ExampleResponseModel();
            response.setResult("successful");
            for (OpenEngSBModelEntry entry : ModelWrapper.create(model).getOpenEngSBModelTail()) {
                if (entry.getKey().equals("specialKey") && entry.getValue().equals("specialValue")) {
                    response.setResult("successful with tail");
                }
            }
            return response;
        }
    }
}
