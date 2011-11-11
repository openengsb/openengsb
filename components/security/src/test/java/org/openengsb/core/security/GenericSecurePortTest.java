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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.SecretKey;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.PrivateKeySource;
import org.openengsb.core.api.security.model.AuthenticationInfo;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

public abstract class GenericSecurePortTest<EncodingType> extends AbstractOpenEngSBTest {

    @Rule
    public TemporaryFolder dataFolder = new TemporaryFolder();

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
    protected AuthenticationManager authManager;
    protected DefaultSecureMethodCallFilterFactory defaultSecureMethodCallFilterFactory;

    @Before
    public void setupInfrastructure() throws Exception {
        System.setProperty("karaf.home", dataFolder.getRoot().getAbsolutePath());
        FileKeySource fileKeySource = new FileKeySource("etc/keys", "RSA");
        fileKeySource.init();
        serverPublicKey = fileKeySource.getPublicKey();
        privateKeySource = fileKeySource;
        requestHandler = mock(RequestHandler.class);
        authManager = mock(AuthenticationManager.class);
        Authentication mockedGoodAuthentication = mock(Authentication.class);
        when(mockedGoodAuthentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(Authentication.class))).thenReturn(mockedGoodAuthentication);
        when(requestHandler.handleCall(any(MethodCall.class))).thenAnswer(new Answer<MethodResult>() {
            @Override
            public MethodResult answer(InvocationOnMock invocation) throws Throwable {
                MethodCall call = (MethodCall) invocation.getArguments()[0];
                return new MethodResult(call.getArgs()[0], call.getMetaData());
            }
        });
        defaultSecureMethodCallFilterFactory = new DefaultSecureMethodCallFilterFactory();
        defaultSecureMethodCallFilterFactory.setAuthenticationManager(authManager);
        defaultSecureMethodCallFilterFactory.setRequestHandler(requestHandler);

        secureRequestHandler = getSecureRequestHandlerFilterChain();
    }

    protected abstract FilterAction getSecureRequestHandlerFilterChain() throws Exception;

    protected abstract EncodingType manipulateMessage(EncodingType encryptedRequest);

    protected abstract EncodingType encodeAndEncrypt(SecureRequest secureRequest, SecretKey sessionKey)
        throws Exception;

    protected abstract SecureResponse decryptAndDecode(EncodingType message, SecretKey sessionKey) throws Exception;

    @Test
    public void processMethodCall_shouldReturnOriginalArgAsResult() throws Exception {
        SecureRequest secureRequest = prepareSecureRequest();

        SecureResponse response = processRequest(secureRequest);

        MethodResultMessage mr = response.getMessage();
        assertThat((String) mr.getResult().getArg(), is(METHOD_ARG));
    }

    protected SecureRequest prepareSecureRequest() {
        return prepareSecureRequest(new UsernamePasswordAuthenticationInfo("test", "password"));
    }

    private SecureRequest prepareSecureRequest(AuthenticationInfo token) {
        MethodCall methodCall = new MethodCall("doSomething", new Object[]{ METHOD_ARG, });
        MethodCallRequest request = new MethodCallRequest(methodCall, "c42");
        SecureRequest secureRequest = SecureRequest.create(request, BeanDescription.fromObject(token));
        return secureRequest;
    }

    @Test
    public void testInvalidAuthentication_shouldNotInvokeRequestHandler() throws Exception {
        when(authManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("bad"));
        SecureRequest secureRequest = prepareSecureRequest();
        try {
            processRequest(secureRequest);
            fail("Expected exception");
        } catch (BadCredentialsException e) {
            // expected, because thrown earlier
        }
        verify(requestHandler, never()).handleCall(any(MethodCall.class));
    }

    @Test
    public void testManipulateMessage_shouldCauseVerificationException() throws Exception {
        SecureRequest secureRequest = prepareSecureRequest();

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
        SecureRequest secureRequest = prepareSecureRequest();

        SecretKey sessionKey = CipherUtils.generateKey("AES", 128);
        EncodingType encryptedRequest = encodeAndEncrypt(secureRequest, sessionKey);
        secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
        secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
    }

    private SecureResponse processRequest(SecureRequest secureRequest) throws Exception {
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
            LOGGER.info(ArrayUtils.toString(o));
        } else {
            LOGGER.info(o.toString());
        }
    }
}
