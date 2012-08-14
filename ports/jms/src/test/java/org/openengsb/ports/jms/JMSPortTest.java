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

package org.openengsb.ports.jms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.PrivateKeySource;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.common.remote.FilterChain;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.JsonMethodCallMarshalFilter;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.openengsb.core.common.remote.XmlDecoderFilter;
import org.openengsb.core.common.remote.XmlMethodCallMarshalFilter;
import org.openengsb.core.common.util.CipherUtils;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.security.filter.EncryptedJsonMessageMarshaller;
import org.openengsb.core.security.filter.JsonSecureRequestMarshallerFilter;
import org.openengsb.core.security.filter.MessageAuthenticatorFilterFactory;
import org.openengsb.core.security.filter.MessageCryptoFilterFactory;
import org.openengsb.core.security.filter.MessageVerifierFilter;
import org.openengsb.core.security.internal.model.ShiroContext;
import org.openengsb.core.services.internal.RequestHandlerImpl;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class JMSPortTest extends AbstractOsgiMockServiceTest {

    private static final String PUBLIC_KEY_64 = ""
            + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEwQedUFElYBNOW71NYLgKEGSqKEbGQ9xhlCjS"
            + "9qd8A7MdaVub61Npc6wSuLJNK1qnrSufWkiZxuo7IsyFnZl9bqkr1D/x4UqKEBmGZIh4s4WIMymw"
            + "TGu2HmAKuKO7JypfQpHemZpLmXTsNse1xFhTfshxWJq4+WqBdeoYZ8p1iwIDAQAB";

    private static final String PRIVATE_KEY_64 = ""
            + "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMTBB51QUSVgE05bvU1guAoQZKoo"
            + "RsZD3GGUKNL2p3wDsx1pW5vrU2lzrBK4sk0rWqetK59aSJnG6jsizIWdmX1uqSvUP/HhSooQGYZk"
            + "iHizhYgzKbBMa7YeYAq4o7snKl9Ckd6ZmkuZdOw2x7XEWFN+yHFYmrj5aoF16hhnynWLAgMBAAEC"
            + "gYEAmyZX+c4e3uke8DhZU04EcjMxHhi6jpdujifF9W147ssAEB5HlfEAinQjaXPpbf7U8soUTwlj"
            + "nJeFlvI+8tIu+J7wuP9m9R/EC02kbYjQUOdmrIXr11GmDNSeKCuklLaQTCKl+eRmVCKk373tmtHE"
            + "/HLAkWsTvdufrkFQi9iaTlECQQDpnHnha5DrcQuUarhwWta+ZDLL56XawfcJZpPfKK2Jgxoqbvg9"
            + "k3i6IRS/kh0g0K98CRK5UvxAiQtDKkDy5z3ZAkEA15xIN5OgfMbE12p83cD4fAU2SpvyzsPk9tTf"
            + "Zb6jnKDAm+hxq1arRyaxL04ppTM/xRRS8DKJLrsAi0HhFzkcAwJAbiuQQyHSX2aZmm3V+46rdXCV"
            + "kBn32rncwf8xP23UoWRFo7tfsNJqfgT53vqOaBpil/FDdkjPk7PNrugvZx5syQJBAJjAEbG+Fu8P"
            + "axkqSjhYpDJJBwOopEa0JhxxB6vveb5XbN2HujAnAMUxtknLWFm/iyg2k+O0Cdhfh60hCTUIsr0C"
            + "QFT8w7k8/FfcAFl+ysJ2lSGpeKkt213QkHpAn2HvHRviVErKSHgEKh10Nf7pU3cgPwHDXNEuQ6Bb"
            + "Ky/vHQD1rMM=";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String METHOD_CALL = ""
            + "{"
            + "  \"classes\":[\"java.lang.String\",\"java.lang.Integer\",\"org.openengsb.ports.jms.TestClass\"],"
            + "  \"methodName\":\"method\","
            + "  \"args\":[\"123\",5,{\"test\":\"test\"}],"
            + "  \"metaData\":{\"serviceId\":\"test\"}"
            + "}";

    private static final String AUTH_DATA = ""
            + "{"
            + "  \"className\":\"org.openengsb.connector.usernamepassword.Password\","
            + "  \"data\":"
            + "  {"
            + "    \"value\":\"password\""
            + "  }"
            + "}";

    private static final String METHOD_CALL_REQUEST = ""
            + "{"
            + "  \"callId\":\"12345\","
            + "  \"answer\":true,"
            + "  \"timestamp\":" + System.currentTimeMillis() + ","
            + "  \"principal\": \"user\","
            + "  \"credentials\":" + AUTH_DATA + ","
            + "  \"methodCall\":" + METHOD_CALL
            + "}";

    private static final String XML_METHOD_CALL_REQUEST = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<MethodCallRequest>"
            + "  <callId>123</callId>"
            + "  <answer>true</answer>"
            + "  <methodCall>"
            + "    <args xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">123</args>"
            + "    <args xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:int\">5</args>"
            + "    <args xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"testClass\">"
            + "      <test>test</test>"
            + "    </args>"
            + "    <classes>java.lang.String</classes>"
            + "    <classes>java.lang.Integer</classes>"
            + "    <classes>org.openengsb.ports.jms.TestClass</classes>"
            + "    <metaData>"
            + "      <entry>"
            + "        <key>serviceId</key>"
            + "        <value>test</value>"
            + "      </entry>"
            + "    </metaData>"
            + "    <methodName>method</methodName>"
            + "  </methodCall>"
            + "</MethodCallRequest>";

    private MethodCallMessage call;
    private MethodResultMessage methodReturn;
    private JMSTemplateFactory jmsTemplateFactory;
    private JMSIncomingPort incomingPort;
    private RequestHandler handler;

    private SimpleMessageListenerContainer simpleMessageListenerConainer;

    private JmsTemplate jmsTemplate;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setup() {
        System.setProperty("org.apache.activemq.default.directory.prefix",
            tempFolder.getRoot().getAbsolutePath() + "/");
        setupKeys();
        String num = UUID.randomUUID().toString();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost" + num);
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplateFactory = new JMSTemplateFactory() {
            @Override
            public SimpleMessageListenerContainer createMessageListenerContainer() {
                return simpleMessageListenerConainer;
            }

            @Override
            public JmsTemplate createJMSTemplate(DestinationUrl destinationUrl) {
                return jmsTemplate;
            }
        };
        simpleMessageListenerConainer = new SimpleMessageListenerContainer();

        incomingPort = new JMSIncomingPort();
        incomingPort.setFactory(jmsTemplateFactory);
        incomingPort.setConnectionFactory(connectionFactory);
        RequestHandlerImpl requestHandlerImpl = new RequestHandlerImpl();
        requestHandlerImpl.setUtilsService(new DefaultOsgiUtilsService(bundleContext));
        handler = requestHandlerImpl;

        TestInterface mock2 = mock(TestInterface.class);
        registerServiceViaId(mock2, "test", TestInterface.class);
        when(mock2.method(anyString(), anyInt(), any(TestClass.class))).thenReturn(new TestClass("test"));

        Map<String, String> metaData = Maps.newHashMap(ImmutableMap.of("serviceId", "test"));
        MethodCall methodCall = new MethodCall("method", new Object[]{ "123", 5, new TestClass("test"), }, metaData);
        call = new MethodCallMessage(methodCall, "123");
        call.setDestination("host?receive");

        MethodResult result = new MethodResult(new TestClass("test"));
        result.setMetaData(metaData);
        methodReturn = new MethodResultMessage(result, "123");
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.PROVIDED_CLASSES_KEY, Password.class.getName());
        props.put(Constants.DELEGATION_CONTEXT_KEY, org.openengsb.core.api.Constants.DELEGATION_CONTEXT_CREDENTIALS);
        registerService(new ClassProviderImpl(bundle, Sets.newHashSet(Password.class.getName())), props,
            ClassProvider.class);
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setAuthenticator(new Authenticator() {
            @Override
            public AuthenticationInfo authenticate(AuthenticationToken authenticationToken)
                throws org.apache.shiro.authc.AuthenticationException {
                return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), authenticationToken
                    .getCredentials(), "openengsb");
            }
        });
        SecurityUtils.setSecurityManager(securityManager);
    }

    private void setupKeys() {
        privateKey = CipherUtils.deserializePrivateKey(Base64.decodeBase64(PRIVATE_KEY_64), "RSA");
        publicKey = CipherUtils.deserializePublicKey(Base64.decodeBase64(PUBLIC_KEY_64), "RSA");
    }

    @Test(timeout = 100000)
    public void testStart_ShouldListenToIncomingCallsAndCallSetRequestHandler() throws Exception {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(JsonMethodCallMarshalFilter.class, new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        String resultString = sendWithTempQueue(METHOD_CALL_REQUEST);

        JsonNode resultMessage = OBJECT_MAPPER.readTree(resultString);
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(), equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), equalTo("{\"test\":\"test\"}"));
    }

    private String sendWithTempQueue(final String msg) {
        String resultString = jmsTemplate.execute(new SessionCallback<String>() {
            @Override
            public String doInJms(Session session) throws JMSException {
                Queue queue = session.createQueue("receive");
                MessageProducer producer = session.createProducer(queue);
                TemporaryQueue tempQueue = session.createTemporaryQueue();
                MessageConsumer consumer = session.createConsumer(tempQueue);
                TextMessage message = session.createTextMessage(msg);
                message.setJMSReplyTo(tempQueue);
                producer.send(message);
                TextMessage response = (TextMessage) consumer.receive(10000);
                assertThat("server should set the value of the correltion ID to the value of the received message id",
                    response.getJMSCorrelationID(), is(message.getJMSMessageID()));
                JmsUtils.closeMessageProducer(producer);
                JmsUtils.closeMessageConsumer(consumer);
                return response != null ? response.getText() : null;
            }
        }, true);
        return resultString;
    }

    @Test(timeout = 60000)
    public void testSendEncryptedMethodCall_shouldSendEncryptedResult() throws Exception {
        FilterChain secureChain = createSecureFilterChain();
        incomingPort.setFilterChain(secureChain);
        incomingPort.start();

        SecretKey sessionKey =
            CipherUtils.generateKey(CipherUtils.DEFAULT_SYMMETRIC_ALGORITHM, CipherUtils.DEFAULT_SYMMETRIC_KEYSIZE);

        byte[] encryptedKey = CipherUtils.encrypt(sessionKey.getEncoded(), publicKey);
        byte[] encryptedContent = CipherUtils.encrypt(METHOD_CALL_REQUEST.getBytes(), sessionKey);

        EncryptedMessage encryptedMessage = new EncryptedMessage(encryptedContent, encryptedKey);
        final String encryptedString = new ObjectMapper().writeValueAsString(encryptedMessage);

        String resultString = sendWithTempQueue(encryptedString);

        byte[] result = CipherUtils.decrypt(Base64.decodeBase64(resultString), sessionKey);
        MethodResultMessage result2 = OBJECT_MAPPER.readValue(result, MethodResultMessage.class);
        MethodResult methodResult = result2.getResult();
        Object realResultArg =
            OBJECT_MAPPER.convertValue(methodResult.getArg(), Class.forName(methodResult.getClassName()));
        assertThat(realResultArg, equalTo((Object) new TestClass("test")));
    }

    private FilterChain createSecureFilterChain() throws Exception {
        AuthenticationDomain authenticationManager = mock(AuthenticationDomain.class);
        when(authenticationManager.authenticate(anyString(), any(Credentials.class))).thenAnswer(
            new Answer<Authentication>() {
                @Override
                public Authentication answer(InvocationOnMock invocation) throws Throwable {
                    String user = (String) invocation.getArguments()[0];
                    Password credentials = (Password) invocation.getArguments()[1];
                    if ("user".equals(user) && credentials.getValue().equals("password")) {
                        return new Authentication(user, credentials.toString());
                    }
                    throw new AuthenticationException("username and password did not match");
                }
            });
        PrivateKeySource keySource = mock(PrivateKeySource.class);
        when(keySource.getPrivateKey()).thenReturn(privateKey);
        MessageCryptoFilterFactory cipherFactory = new MessageCryptoFilterFactory(keySource, "AES");
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            EncryptedJsonMessageMarshaller.class,
            cipherFactory,
            JsonSecureRequestMarshallerFilter.class,
            MessageVerifierFilter.class,
            new MessageAuthenticatorFilterFactory(new DefaultOsgiUtilsService(bundleContext), new ShiroContext()),
            new RequestMapperFilter(handler)));
        FilterChain secureChain = factory.create();
        return secureChain;
    }

    @Test(timeout = 5000)
    public void testPortWithXmlFormat_shouldWorkWithXmlFilterChain() throws Exception {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            XmlDecoderFilter.class,
            XmlMethodCallMarshalFilter.class,
            new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        String resultString = sendWithTempQueue(XML_METHOD_CALL_REQUEST);

        assertThat(resultString, containsString("<callId>123</callId>"));
        assertThat(resultString, containsString("<type>Object</type>"));
        assertThat(resultString, containsString("<test>test</test>"));
    }

    @Test
    public void testStop_shouldNotReactToIncomingCalls() throws Exception {
        SimpleMessageListenerContainer orig = simpleMessageListenerConainer;
        SimpleMessageListenerContainer containerSpy = spy(orig);
        simpleMessageListenerConainer = containerSpy;

        ConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost2");
        incomingPort.setConnectionFactory(cf);
        incomingPort.start();
        incomingPort.stop();
        verify(containerSpy).stop();
    }

    @Test
    public void testRequestMapping_shouldDeserialiseRequest() throws Exception {
        OBJECT_MAPPER.readValue(METHOD_CALL_REQUEST, MethodCallMessage.class);
    }

    @Test
    public void testMethodReturn_shouldDeserialiseResponse() throws Exception {
        StringWriter writer = new StringWriter();
        OBJECT_MAPPER.writeValue(writer, methodReturn);
        JsonNode resultMessage = OBJECT_MAPPER.readTree(writer.toString());
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(), equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), equalTo("{\"test\":\"test\"}"));
    }
}
