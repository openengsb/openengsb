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

package org.openengsb.core.common.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.security.MessageVerificationFailedException;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo;
import org.openengsb.core.common.security.filter.DefaultSecureMethodCallFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

public abstract class GenericSecurePortTest<EncodingType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSecurePortTest.class);

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

    protected FilterAction secureRequestHandler;
    protected FilterAction requestHandler;
    protected KeyGeneratorUtils keyGenUtil;
    protected BinaryMessageCryptoUtil cryptoUtil;
    protected PublicKey serverPublicKey;
    protected PrivateKey serverPrivateKey;
    protected AuthenticationManager authManager;
    protected DefaultSecureMethodCallFilterFactory defaultSecureMethodCallFilterFactory;

    @SuppressWarnings("unchecked")
    @Before
    public void setupInfrastructure() throws Exception {
        cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());
        keyGenUtil = new KeyGeneratorUtils(AlgorithmConfig.getDefault());
        KeySerializationUtil keySerializeUtil = new KeySerializationUtil(AlgorithmConfig.getDefault());
        serverPublicKey = keySerializeUtil.deserializePublicKey(Base64.decodeBase64(PUBLIC_KEY_64));
        serverPrivateKey = keySerializeUtil.deserializePrivateKey(Base64.decodeBase64(PRIVATE_KEY_64));
        requestHandler = mock(FilterAction.class);
        authManager = mock(AuthenticationManager.class);
        when(requestHandler.filter(any(MethodCall.class), any(Map.class))).thenAnswer(new Answer<MethodResult>() {
            @Override
            public MethodResult answer(InvocationOnMock invocation) throws Throwable {
                MethodCall call = (MethodCall) invocation.getArguments()[0];
                return new MethodResult(call.getArgs()[0]);
            }
        });
        defaultSecureMethodCallFilterFactory = new DefaultSecureMethodCallFilterFactory();
        defaultSecureMethodCallFilterFactory.setAuthenticationManager(authManager);
        defaultSecureMethodCallFilterFactory.setHandler(requestHandler);

        secureRequestHandler = getSecureRequestHandlerFilterChain();
    }

    protected abstract FilterAction getSecureRequestHandlerFilterChain() throws Exception;

    protected abstract EncodingType encodeAndEncrypt(SecureRequest secureRequest, SecretKey sessionKey)
        throws Exception;

    protected abstract SecureResponse decryptAndDecode(EncodingType message, SecretKey sessionKey) throws Exception;

    @Test
    public void testDefaultImpls() throws Exception {
        SecureRequest secureRequest = prepareSecureRequest();
        SecureResponse response = processRequest(secureRequest);

        MethodResult mr = response.getMessage();
        assertThat((String) mr.getArg(), is("42"));
    }

    private SecureRequest prepareSecureRequest() {
        UsernamePasswordAuthenticationInfo token = new UsernamePasswordAuthenticationInfo("test", "password");
        MethodCall request = new MethodCall("doSomething", new Object[]{ "42", }, new HashMap<String, String>());
        SecureRequest secureRequest = SecureRequest.create(request, BeanDescription.fromObject(token));
        return secureRequest;
    }

    @Test
    public void testInvalidAuthentication() throws Exception {
        UsernamePasswordAuthenticationInfo token = new UsernamePasswordAuthenticationInfo("test", "password");
        when(authManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("bad"));
        MethodCall request = new MethodCall("doSomething", new Object[]{ "42", }, new HashMap<String, String>());
        SecureRequest secureRequest = SecureRequest.create(request, BeanDescription.fromObject(token));

        try {
            processRequest(secureRequest);
            fail("Expected exception");
        } catch (BadCredentialsException e) {
            // expected, because thrown earlier
        }
        verify(requestHandler, never()).filter(any(MethodCall.class), any(Map.class));
    }

    @Test
    public void testManipulateMessage() throws Exception {
        SecureRequest secureRequest = prepareSecureRequest();

        secureRequest.getMessage().setArgs(new Object[]{ "43" }); // manipulate message

        try {
            processRequest(secureRequest);
            fail("Exception expected");
        } catch (FilterException e) {
            assertThat(e.getCause(), is(MessageVerificationFailedException.class));
        }
    }

    @Test
    public void testReplayMessage_shouldBeRejected() throws Exception {
        SecureRequest secureRequest = prepareSecureRequest();

        SecretKey sessionKey = keyGenUtil.generateKey();
        EncodingType encryptedRequest = encodeAndEncrypt(secureRequest, sessionKey);
        secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
        secureRequestHandler.filter(encryptedRequest, new HashMap<String, Object>());
    }

    private SecureResponse processRequest(SecureRequest secureRequest) throws Exception {
        SecretKey sessionKey = keyGenUtil.generateKey();
        EncodingType encryptedRequest = encodeAndEncrypt(secureRequest, sessionKey);
        logRequest(encryptedRequest);
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
