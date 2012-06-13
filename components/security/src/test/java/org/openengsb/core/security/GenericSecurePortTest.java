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

package org.openengsb.core.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.MessageVerificationFailedException;
import org.openengsb.core.api.security.PrivateKeySource;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.openengsb.core.common.util.CipherUtils;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.security.filter.MessageAuthenticatorFilterFactory;
import org.openengsb.core.security.filter.MessageVerifierFilter;
import org.openengsb.core.security.internal.FileKeySource;
import org.openengsb.core.security.internal.OpenEngSBSecurityManager;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.rules.DedicatedThread;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public abstract class GenericSecurePortTest<EncodingType> extends AbstractOsgiMockServiceTest {

    @Rule
    public TemporaryFolder dataFolder = new TemporaryFolder();

    @Rule
    public DedicatedThread dedicatedThread = new DedicatedThread();

    private static final String LOREM_IPSUM = ""
            + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut "
            + "labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo "
            + "dolores et ea rebum.";

    private static final String TEST_UMLAUTS = " äöüßêéèỳíóæðÐł€øØãẽĩõũ";

    private static final String METHOD_ARG = StringUtils.repeat(LOREM_IPSUM + TEST_UMLAUTS, 2);

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSecurePortTest.class);

    protected PrivateKeySource privateKeySource;

    protected FilterAction secureRequestHandler;
    protected RequestHandler requestHandler;
    protected PublicKey serverPublicKey;
    protected AuthenticationDomain authManager;

    protected FilterChainFactory<MethodCallMessage, MethodResultMessage> filterTop;

    @After
    public void cleanupShiro() {
        ThreadContext.unbindSecurityManager();
        ThreadContext.unbindSubject();
    }

    @Before
    public void setupInfrastructure() throws Exception {
        System.setProperty("karaf.home", dataFolder.getRoot().getAbsolutePath());
        FileKeySource fileKeySource = new FileKeySource("etc/keys", "RSA");
        fileKeySource.init();
        serverPublicKey = fileKeySource.getPublicKey();
        privateKeySource = fileKeySource;
        requestHandler = mock(RequestHandler.class);
        authManager = mock(AuthenticationDomain.class);
        when(authManager.authenticate(anyString(), any(Credentials.class))).thenAnswer(new Answer<Authentication>() {
            @Override
            public Authentication answer(InvocationOnMock invocation) throws Throwable {
                String username = (String) invocation.getArguments()[0];
                Credentials credentials = (Credentials) invocation.getArguments()[1];
                return new Authentication(username, credentials);
            }
        });
        setupSecurityManager();
        when(requestHandler.handleCall(any(MethodCall.class))).thenAnswer(new Answer<MethodResult>() {
            @Override
            public MethodResult answer(InvocationOnMock invocation) throws Throwable {
                MethodCall call = (MethodCall) invocation.getArguments()[0];
                return new MethodResult(call.getArgs()[0], call.getMetaData());
            }
        });

        FilterChainFactory<MethodCallMessage, MethodResultMessage> factory =
            new FilterChainFactory<MethodCallMessage, MethodResultMessage>(MethodCallMessage.class,
                MethodResultMessage.class);
        List<Object> filterFactories = new LinkedList<Object>();
        filterFactories.add(MessageVerifierFilter.class);
        filterFactories.add(new MessageAuthenticatorFilterFactory(new DefaultOsgiUtilsService(bundleContext)));
        filterFactories.add(new RequestMapperFilter(requestHandler));
        factory.setFilters(filterFactories);
        factory.create();

        filterTop = factory;

        secureRequestHandler = getSecureRequestHandlerFilterChain();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.PROVIDED_CLASSES_KEY, Password.class.getName());
        props.put(Constants.DELEGATION_CONTEXT_KEY, org.openengsb.core.api.Constants.DELEGATION_CONTEXT_CREDENTIALS);
        registerService(new ClassProviderImpl(bundle, Sets.newHashSet(Password.class.getName())), props,
            ClassProvider.class);
    }

    private void setupSecurityManager() {
        OpenEngSBSecurityManager openEngSBSecurityManager = new OpenEngSBSecurityManager();
        OpenEngSBShiroAuthenticator authenticator = new OpenEngSBShiroAuthenticator();
        authenticator.setAuthenticator(authManager);
        openEngSBSecurityManager.setAuthenticator(authenticator);
        SecurityUtils.setSecurityManager(openEngSBSecurityManager);
    }

    protected abstract FilterAction getSecureRequestHandlerFilterChain() throws Exception;

    protected abstract EncodingType manipulateMessage(EncodingType encryptedRequest);

    protected abstract EncodingType encodeAndEncrypt(MethodCallMessage secureRequest, SecretKey sessionKey)
        throws Exception;

    protected abstract MethodResultMessage decryptAndDecode(EncodingType message, SecretKey sessionKey)
        throws Exception;

    @Test
    public void processMethodCall_shouldReturnOriginalArgAsResult() throws Exception {
        MethodCallMessage secureRequest = prepareSecureRequest();

        MethodResultMessage response = processRequest(secureRequest);

        MethodResultMessage mr = response;
        assertThat((String) mr.getResult().getArg(), is(METHOD_ARG));
    }

    protected MethodCallMessage prepareSecureRequest() {
        return prepareSecureRequest("test", new Password("password"));
    }

    private MethodCallMessage prepareSecureRequest(String username, Object credentials) {
        MethodCall methodCall = new MethodCall("doSomething", new Object[]{ METHOD_ARG, });
        MethodCallMessage request = new MethodCallMessage(methodCall, "c42");
        request.setPrincipal(username);
        request.setCredentials(BeanDescription.fromObject(credentials));
        return request;
    }

    @Test
    public void testInvalidAuthentication_shouldNotInvokeRequestHandler() throws Exception {
        when(authManager.authenticate(anyString(), any(Credentials.class))).thenThrow(
            new AuthenticationException("bad"));
        MethodCallMessage secureRequest = prepareSecureRequest();
        try {
            processRequest(secureRequest);
            fail("Expected exception");
        } catch (FilterException e) {
            assertThat(e.getCause(), is(org.apache.shiro.authc.AuthenticationException.class));
        }
        verify(requestHandler, never()).handleCall(any(MethodCall.class));
    }

    @Test
    public void testManipulateMessage_shouldCauseVerificationException() throws Exception {
        MethodCallMessage secureRequest = prepareSecureRequest();

        SecretKey sessionKey = CipherUtils.generateKey("AES", 128);
        EncodingType encryptedRequest = encodeAndEncrypt(secureRequest, sessionKey);

        logRequest(encryptedRequest);
        EncodingType manipulatedRequest = manipulateMessage(encryptedRequest);

        try {
            secureRequestHandler.filter(manipulatedRequest, new HashMap<String, Object>());
            fail("Exception expected");
        } catch (FilterException e) {
            verify(requestHandler, never()).handleCall(any(MethodCall.class));
        }

    }

    @Test
    public void testReplayMessage_shouldBeRejected() throws Exception {
        MethodCallMessage secureRequest = prepareSecureRequest();
        SecretKey sessionKey = CipherUtils.generateKey("AES", 128);
        EncodingType encryptedRequest = encodeAndEncrypt(secureRequest, sessionKey);
        secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
        try {
            secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
            fail("replay was not detected");
        } catch (FilterException e) {
            assertThat(e.getCause(), is(MessageVerificationFailedException.class));
        }
    }

    private MethodResultMessage processRequest(MethodCallMessage secureRequest) throws Exception {
        SecretKey sessionKey = CipherUtils.generateKey("AES", 128);
        EncodingType encryptedRequest = encodeAndEncrypt(secureRequest, sessionKey);
        logRequest(encryptedRequest);
        @SuppressWarnings("unchecked")
        EncodingType encodedResponse =
            (EncodingType) secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
        logRequest(encodedResponse);
        return decryptAndDecode(encodedResponse, sessionKey);
    }

    private static void logRequest(Object o) {
        if (o.getClass().isArray()) {
            LOGGER.trace(ArrayUtils.toString(o));
        } else {
            LOGGER.trace(o.toString());
        }
    }
}
