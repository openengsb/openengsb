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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.remote.GenericObjectSerializer;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.PrivateKeySource;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.JsonObjectSerializer;
import org.openengsb.core.common.remote.CustomAnnotationReader;
import org.openengsb.core.common.remote.FilterChain;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.JsonMethodCallMarshalFilterFactory;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.openengsb.core.common.remote.XmlMethodCallMarshalFilter;
import org.openengsb.core.common.util.CipherUtils;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.security.filter.EncryptedJsonMessageMarshaller;
import org.openengsb.core.security.filter.JsonSecureRequestMarshallerFilterFactory;
import org.openengsb.core.security.filter.MessageAuthenticatorFilter;
import org.openengsb.core.security.filter.MessageCryptoFilterFactory;
import org.openengsb.core.security.filter.MessageVerifierFilter;
import org.openengsb.core.security.filter.WrapperFilter;
import org.openengsb.core.services.internal.RequestHandlerImpl;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.internal.ClassProviderWithAliases;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.xml.bind.api.JAXBRIContext;

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

    private static final String METHOD_CALL = ""
            + "{"
            + "  \"methodName\" : \"method\","
            + "  \"args\" : [ \"123\", 5, {"
            + "    \"@type\" : \"TestClass\","
            + "    \"test\" : \"test\""
            + "  } ],"
            + "  \"metaData\" : {"
            + "    \"serviceId\" : \"test\""
            + "  }"
            + "}";

    private static final String METHOD_CALL_REQUEST = ""
            + "{"
            + "  \"callId\":\"12345\","
            + "  \"answer\":true,"
            + "  \"methodCall\":" + METHOD_CALL
            + "}";

    private static final String AUTH_DATA = ""
            + "  {"
            + "    \"@type\": \"Password\","
            + "    \"value\":\"password\""
            + "  }";

    private static final String SECURE_METHOD_CALL = ""
            + "{"
            + "  \"timestamp\":" + System.currentTimeMillis() + ","
            + "  \"principal\": \"user\","
            + "  \"credentials\": " + AUTH_DATA + ","
            + "  \"message\":" + METHOD_CALL_REQUEST
            + "}";

    private static final String XML_METHOD_CALL_REQUEST =
        ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<methodCallRequest>"
                + "    <answer>true</answer>"
                + "    <callId>123</callId>"
                + "    <destination>host?receive</destination>"
                + "    <methodCall>"
                + "        <args>"
                + "            <arg xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
                + "              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">123</arg>"
                + "            <arg xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
                + "              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">5</arg>"
                + "            <arg xsi:type=\"testClass\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "                <test>test</test>"
                + "            </arg>"
                + "        </args>"
                + "        <metaData>"
                + "            <entry>"
                + "                <key>serviceId</key>"
                + "                <value>test</value>"
                + "            </entry>"
                + "        </metaData>"
                + "        <methodName>method</methodName>"
                + "    </methodCall>"
                + "</methodCallRequest>";

    private MethodCallRequest call;
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

    private GenericObjectSerializer objectSerializer;

    private GenericObjectSerializer makeJsonObjectSerializer() {
        JsonObjectSerializer jsonObjectSerializer = new JsonObjectSerializer();
        jsonObjectSerializer.setBundleContext(bundleContext);
        jsonObjectSerializer.init();
        return jsonObjectSerializer;
    }

    @Ignore
    @Test
    public void createMessage() throws Exception {
        System.out.println(objectSerializer.serializeToString(call.getMethodCall()));

        Class<?>[] clazzes = new Class<?>[]{
            SecureRequest.class,
            SecureResponse.class,
            MethodCallRequest.class,
            MethodResultMessage.class,
            MethodCall.class,
            MethodResult.class,
            TestClass.class,
            Password.class,
        };
        JAXBContext jaxbContext =
            JAXBContext.newInstance(clazzes,
                ImmutableMap.of(JAXBRIContext.ANNOTATION_READER, new CustomAnnotationReader()));

        StringWriter writer = new StringWriter();
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(call, writer);
        String xmlstring = writer.toString();
        System.out.println(xmlstring);

        jaxbContext = JAXBContext.newInstance(clazzes);

        SecureRequest request = SecureRequest.create(call, "admin", new Password("password"));
        StringWriter requestWriter = new StringWriter();
        marshaller.marshal(request, requestWriter);
        String requestString = requestWriter.toString();

        System.out.println(requestString);
    }

    @Test
    public void parseMessage() throws Exception {
        Class<?>[] clazzes = new Class<?>[]{
            SecureRequest.class,
            SecureResponse.class,
            MethodCallRequest.class,
            MethodResultMessage.class,
            MethodCall.class,
            MethodResult.class,
            TestClass.class,
            Password.class,
        };

        JAXBContext newInstance = JAXBContext.newInstance(clazzes,
            ImmutableMap.of(JAXBRIContext.ANNOTATION_READER, new CustomAnnotationReader()));
        Unmarshaller unmarshaller = newInstance.createUnmarshaller();
        MethodCallRequest unmarshaled =
            (MethodCallRequest) unmarshaller.unmarshal(new StringReader(XML_METHOD_CALL_REQUEST));
        System.out.println(unmarshaled);
    }

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
        call = new MethodCallRequest(methodCall, "123");
        call.setDestination("host?receive");

        MethodResult result = new MethodResult(new TestClass("test"));
        result.setMetaData(metaData);
        methodReturn = new MethodResultMessage(result, "123");

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        props.put(Constants.PROVIDED_CLASSES_KEY, Arrays.asList(Password.class.getName(), "Password"));
        props.put(Constants.DELEGATION_CONTEXT, org.openengsb.core.api.Constants.DELEGATION_CONTEXT_CREDENTIALS);
        ClassProviderWithAliases providerWithAliases =
            new ClassProviderWithAliases(bundle, Arrays.asList(Password.class.getName()), ImmutableMap.of("Password",
                Password.class.getName()));
        registerService(providerWithAliases, props, ClassProvider.class);

        props = new Hashtable<String, Object>();
        props.put(Constants.PROVIDED_CLASSES_KEY, Arrays.asList(TestClass.class.getName(), "TestClass"));
        registerService(
            new ClassProviderWithAliases(bundle, Sets.newHashSet(TestClass.class.getName()), ImmutableMap.of(
                "TestClass", TestClass.class.getName())), props, ClassProvider.class);

        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setAuthenticator(new Authenticator() {
            @Override
            public AuthenticationInfo authenticate(org.apache.shiro.authc.AuthenticationToken authenticationToken)
                throws org.apache.shiro.authc.AuthenticationException {
                return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), authenticationToken
                    .getCredentials(), "openengsb");
            }
        });
        SecurityUtils.setSecurityManager(securityManager);

        objectSerializer = makeJsonObjectSerializer();
    }

    private void setupKeys() {
        privateKey = CipherUtils.deserializePrivateKey(Base64.decodeBase64(PRIVATE_KEY_64), "RSA");
        publicKey = CipherUtils.deserializePublicKey(Base64.decodeBase64(PUBLIC_KEY_64), "RSA");
    }

    @Test(timeout = 100000)
    public void start_ShouldListenToIncomingCallsAndCallSetRequestHandler() throws InterruptedException, IOException {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        JsonMethodCallMarshalFilterFactory marshalFilterFactory =
            new JsonMethodCallMarshalFilterFactory(objectSerializer);
        factory.setFilters(Arrays.asList(marshalFilterFactory, new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        String resultString = sendWithTempQueue(METHOD_CALL_REQUEST);
        System.out.println(resultString);
        MethodResultMessage resultMessage = objectSerializer.parse(resultString, MethodResultMessage.class);
        assertThat(resultMessage.getResult().getMetaData().get("serviceId"), is("test"));
        assertThat((TestClass) resultMessage.getResult().getArg(), is(new TestClass("test")));
        assertThat(resultMessage.getResult().getType(), is(MethodResult.ReturnType.Object));
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
                TextMessage response = (TextMessage) consumer.receive(60000);
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
    public void sendEncryptedMethodCall_shouldSendEncryptedResult() throws Exception {
        FilterChain secureChain = createSecureFilterChain();
        incomingPort.setFilterChain(secureChain);
        incomingPort.start();

        SecretKey sessionKey =
            CipherUtils.generateKey(CipherUtils.DEFAULT_SYMMETRIC_ALGORITHM, CipherUtils.DEFAULT_SYMMETRIC_KEYSIZE);

        byte[] encryptedKey = CipherUtils.encrypt(sessionKey.getEncoded(), publicKey);
        byte[] encryptedContent = CipherUtils.encrypt(SECURE_METHOD_CALL.getBytes(), sessionKey);

        EncryptedMessage encryptedMessage = new EncryptedMessage(encryptedContent, encryptedKey);
        final String encryptedString = objectSerializer.serializeToString(encryptedMessage);

        String resultString = sendWithTempQueue(encryptedString);

        byte[] result = CipherUtils.decrypt(Base64.decodeBase64(resultString), sessionKey);
        SecureResponse result2 = objectSerializer.parse(result, SecureResponse.class);
        MethodResult methodResult = result2.getMessage().getResult();

        assertThat(methodResult.getArg(), equalTo((Object) new TestClass("test")));
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
        JsonSecureRequestMarshallerFilterFactory marshallerFilterFactory =
            new JsonSecureRequestMarshallerFilterFactory(objectSerializer);
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            EncryptedJsonMessageMarshaller.class,
            cipherFactory,
            marshallerFilterFactory,
            MessageVerifierFilter.class,
            MessageAuthenticatorFilter.class,
            WrapperFilter.class,
            new RequestMapperFilter(handler)));
        FilterChain secureChain = factory.create();
        return secureChain;
    }

    @Ignore
    @Test(timeout = 50000)
    public void testPortWithXmlFormat_shouldWorkWithXmlFilterChain() throws InterruptedException, IOException {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
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
    public void stop_ShouldNotReactToIncomingCalls() {
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
    public void requestMapping_shouldDeserialiseRequest() throws IOException {
        objectSerializer.parse(METHOD_CALL_REQUEST, MethodCallRequest.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws IOException {
        String resultString = objectSerializer.serializeToString(methodReturn);
        JsonNode resultMessage = new ObjectMapper().readTree(resultString);
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("type").asText(), is("Object"));
        assertThat(readTree.get("arg").get("@type").asText(), is("TestClass"));
        assertThat(readTree.get("arg").get("test").asText(), is("test"));
        assertThat(readTree.get("metaData").get("@type"), nullValue());
        assertThat(readTree.get("metaData").get("serviceId").asText(), is("test"));
        System.out.println(resultString);

    }

}
