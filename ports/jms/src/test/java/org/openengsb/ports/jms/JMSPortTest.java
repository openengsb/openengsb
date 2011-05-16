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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.remote.FilterChain;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.JsonMethodCallMarshalFilter;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.openengsb.core.common.remote.XmlDecoderFilter;
import org.openengsb.core.common.remote.XmlMethodCallMarshalFilter;
import org.openengsb.core.common.security.AlgorithmConfig;
import org.openengsb.core.common.security.BinaryMessageCryptoUtil;
import org.openengsb.core.common.security.KeyGeneratorUtils;
import org.openengsb.core.common.security.KeySerializationUtil;
import org.openengsb.core.common.security.PrivateKeySource;
import org.openengsb.core.common.security.filter.DefaultSecureMethodCallFilterFactory;
import org.openengsb.core.common.security.filter.EncryptedJsonMessageMarshaller;
import org.openengsb.core.common.security.filter.JsonSecureRequestMarshallerFilter;
import org.openengsb.core.common.security.filter.MessageCryptoFilterFactory;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.services.internal.RequestHandlerImpl;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

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

    private static final String METHOD_CALL_REQUEST = ""
            + "{"
            + "  \"callId\":\"12345\","
            + "  \"answer\":true,"
            + "  \"methodCall\":" + METHOD_CALL
            + "}";

    private static final String AUTH_DATA = ""
            + "{"
            + "  \"className\":\"org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo\","
            + "  \"data\":"
            + "  {"
            + "    \"username\":\"user\","
            + "    \"password\":\"password\""
            + "  }"
            + "}";

    private static final String SECURE_METHOD_CALL = ""
            + "{"
            + "  \"timestamp\":" + System.currentTimeMillis() + ","
            + "  \"authenticationData\":" + AUTH_DATA + ","
            + "  \"message\":" + METHOD_CALL_REQUEST
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

    private MethodCallRequest call;
    private MethodResultMessage methodReturn;
    private JMSTemplateFactory jmsTemplateFactory;
    private JMSIncomingPort incomingPort;
    private RequestHandler handler;

    private SimpleMessageListenerContainer simpleMessageListenerConainer;

    private JmsTemplate jmsTemplate;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Before
    public void setup() {

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

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
        handler = new RequestHandlerImpl();

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
    }

    private void setupKeys() {
        KeySerializationUtil serializationUtil = new KeySerializationUtil(AlgorithmConfig.getDefault());
        privateKey = serializationUtil.deserializePrivateKey(Base64.decodeBase64(PRIVATE_KEY_64));
        publicKey = serializationUtil.deserializePublicKey(Base64.decodeBase64(PUBLIC_KEY_64));
    }

    @Test(timeout = 5000)
    public void start_ShouldListenToIncomingCallsAndCallSetRequestHandler() throws InterruptedException, IOException {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            JsonMethodCallMarshalFilter.class,
            new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        jmsTemplate.convertAndSend("receive", METHOD_CALL_REQUEST);
        String resultString = (String) jmsTemplate.receiveAndConvert("12345");
        JsonNode resultMessage = OBJECT_MAPPER.readTree(resultString);
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(), equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), equalTo("{\"test\":\"test\"}"));
    }

    @Test(timeout = 5000)
    public void sendEncryptedMethodCall_shouldSendEncryptedResult() throws Exception {
        FilterChain secureChain = createSecureFilterChain();
        incomingPort.setFilterChain(secureChain);
        incomingPort.start();

        KeyGeneratorUtils keyGeneratorUtils = new KeyGeneratorUtils(AlgorithmConfig.getDefault());
        SecretKey sessionKey = keyGeneratorUtils.generateKey();

        MessageCryptoUtil<byte[]> cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());

        byte[] encryptedKey = cryptoUtil.encryptKey(sessionKey, publicKey);
        byte[] encryptedContent = cryptoUtil.encrypt(SECURE_METHOD_CALL.getBytes(), sessionKey);

        EncryptedMessage encryptedMessage = new EncryptedMessage(encryptedContent, encryptedKey);
        String encryptedString = new ObjectMapper().writeValueAsString(encryptedMessage);

        jmsTemplate.convertAndSend("receive", encryptedString);
        String resultString = (String) jmsTemplate.receiveAndConvert("12345");
        byte[] result = cryptoUtil.decrypt(Base64.decodeBase64(resultString), sessionKey);
        SecureResponse result2 = OBJECT_MAPPER.readValue(result, SecureResponse.class);
        MethodResult methodResult = result2.getMessage().getResult();
        Object realResultArg =
            OBJECT_MAPPER.convertValue(methodResult.getArg(), Class.forName(methodResult.getClassName()));
        assertThat(realResultArg, equalTo((Object) new TestClass("test")));
    }

    private FilterChain createSecureFilterChain() {
        DefaultSecureMethodCallFilterFactory secureFilterChainFactory = new DefaultSecureMethodCallFilterFactory();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenAnswer(new Answer<Authentication>() {
            @Override
            public Authentication answer(InvocationOnMock invocation) throws Throwable {
                UsernamePasswordAuthenticationToken token =
                    (UsernamePasswordAuthenticationToken) invocation.getArguments()[0];
                if (token.getPrincipal().equals("user") && token.getCredentials().equals("password")) {
                    Authentication authMock = mock(Authentication.class);
                    when(authMock.isAuthenticated()).thenReturn(true);
                    return authMock;
                }
                throw new BadCredentialsException("username and password did not match");
            }
        });
        secureFilterChainFactory.setAuthenticationManager(authenticationManager);

        secureFilterChainFactory.setRequestHandler(handler);
        PrivateKeySource keySource = mock(PrivateKeySource.class);
        when(keySource.getPrivateKey()).thenReturn(privateKey);
        MessageCryptoFilterFactory cipherFactory = new MessageCryptoFilterFactory(keySource);

        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            EncryptedJsonMessageMarshaller.class,
            cipherFactory,
            JsonSecureRequestMarshallerFilter.class,
            secureFilterChainFactory.create()));

        FilterChain secureChain = factory.create();
        return secureChain;
    }

    @Test(timeout = 5000)
    public void testPortWithXmlFormat_shouldWorkWithXmlFilterChain() throws InterruptedException, IOException {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            XmlDecoderFilter.class,
            XmlMethodCallMarshalFilter.class,
            new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        jmsTemplate.convertAndSend("receive", XML_METHOD_CALL_REQUEST);
        String resultString = (String) jmsTemplate.receiveAndConvert("123");

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
        OBJECT_MAPPER.readValue(METHOD_CALL_REQUEST, MethodCallRequest.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws IOException {
        StringWriter writer = new StringWriter();
        OBJECT_MAPPER.writeValue(writer, methodReturn);
        JsonNode resultMessage = OBJECT_MAPPER.readTree(writer.toString());
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(), equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), equalTo("{\"test\":\"test\"}"));

    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
    }
}
