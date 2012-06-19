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
import static org.hamcrest.CoreMatchers.nullValue;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.util.JsonUtils;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.remoteclient.SecureSampleConnector;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class JMSPortIT extends AbstractRemoteTestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSPortIT.class);
    private DefaultOsgiUtilsService utilsService;

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
        addWorkflow("simpleFlow");
        String string = null;
        while (string == null) {
            // TODO OPENENGSB-2097 find a better way than an endless loop
            LOGGER.warn("checking for simpleFlow to be present");
            string = ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, "simpleFlow"));
            Thread.sleep(1000);
        }

        utilsService = new DefaultOsgiUtilsService(getBundleContext());
    }

    @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId = utilsService.getServiceWithId(OutgoingPort.class, "jms-json", 60000);
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
        authenticateAsAdmin();
        // make sure security-stuff is off
        System.setProperty("org.openengsb.jms.noencrypt", "true");
        System.setProperty("org.openengsb.security.noverify", "true");

        // make sure jms is up and running
        utilsService.getServiceWithId(OutgoingPort.class, "jms-json", 60000);

        SecureSampleConnector remoteConnector = new SecureSampleConnector();
        remoteConnector.start();
        ExampleDomain osgiService = getOsgiService(ExampleDomain.class, "(service.pid=example-remote)", 31000);

        assertThat(getBundleContext().getServiceReferences(ExampleDomain.class.getName(),
            "(service.pid=example-remote)"), not(nullValue()));
        assertThat(osgiService, not(nullValue()));

        remoteConnector.getInvocationHistory().clear();
        osgiService.doSomethingWithMessage("test");
        assertThat(remoteConnector.getInvocationHistory().isEmpty(), is(false));

        remoteConnector.stop();
        Thread.sleep(5000);
        assertThat(getBundleContext().getServiceReferences(ExampleDomain.class.getName(),
            "(id=example-remote)"), nullValue());
    }

    @Test
    public void testSendMethodWithModelAsParamter_shouldWork() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.root", new String[]{ "foo" });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        JmsTemplate template = prepareActiveMqConnection();
        String secureRequest = prepareRequest(METHOD_CALL_WITH_MODEL_PARAMETER, "admin", "password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        String result = sendMessage(template, encryptedMessage);
        String decryptedResult = decryptResult(sessionKey, result);

        ObjectMapper mapper = new ObjectMapper();
        MethodResultMessage response = mapper.readValue(decryptedResult, MethodResultMessage.class);
        MethodResultMessage methodResult = response;
        JsonUtils.convertResult(methodResult);
        OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) methodResult.getResult().getArg();
        ExampleResponseModel model = (ExampleResponseModel) ModelUtils.generateModelOutOfWrapper(wrapper);

        assertThat(decryptedResult.contains("successful"), is(true));
        assertThat(wrapper.getModelClass(), is(ExampleResponseModel.class.getName()));
        assertThat(model.getResult(), is("successful"));
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
            ExampleResponseModel response = ModelUtils.createEmptyModelObject(ExampleResponseModel.class);
            response.setResult("successful");
            return response;
        }


    }

}
