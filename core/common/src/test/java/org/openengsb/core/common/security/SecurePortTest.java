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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.SecretKey;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.MessageVerificationFailedException;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class SecurePortTest {

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

    private SecureRequestHandler<byte[]> secureRequestHandler;

    private SecretKeyUtil secretKeyUtil;

    private PublicKeyUtil publicKeyUtil;

    private PublicKeyCipherUtil publicKeyCipherUtil;

    private SecretKeyCipherUtil secretKeyCipherUtil;

    private PublicKey serverPublicKey;

    private PrivateKey serverPrivateKey;

    private AuthenticationManager authManager;

    private RequestHandler realHandler;

    @Before
    public void setUp() throws Exception {
        makeSecureHandler();
        secretKeyUtil = new SecretKeyUtil();
        publicKeyUtil = new PublicKeyUtil();
        publicKeyCipherUtil = new PublicKeyCipherUtil();
        secretKeyCipherUtil = new SecretKeyCipherUtil();

        serverPublicKey = publicKeyUtil.deserializePublicKey(PUBLIC_KEY_64);
        serverPrivateKey = publicKeyUtil.deserializePrivateKey(PRIVATE_KEY_64);

        setupRequestHandler();
    }

    private void makeSecureHandler() {
        secureRequestHandler = new SecureRequestHandler<byte[]>() {
            @Override
            public SecureRequest unmarshalRequest(byte[] input) {
                return (SecureRequest) SerializationUtils.deserialize(input);
            }

            @SuppressWarnings("unchecked")
            @Override
            public EncryptedMessage<byte[]> unmarshalContainer(byte[] container) {
                return (EncryptedMessage<byte[]>) SerializationUtils.deserialize(container);
            }

            @Override
            public byte[] marshalResponse(SecureResponse response) {
                return SerializationUtils.serialize(response);
            }
        };
    }

    @Test
    public void testDefaultImpls() throws Exception {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "password");
        MethodCall request = new MethodCall("doSomething", new Object[]{ "42", }, new HashMap<String, String>());
        SecureRequest secureRequest = SecureRequest.create(request, token);

        SecretKey sessionKey = secretKeyUtil.generateKey(128);

        byte[] serializedRequest = SerializationUtils.serialize(secureRequest);
        byte[] encryptedRequest = secretKeyCipherUtil.encrypt(serializedRequest, sessionKey);

        byte[] encodedKey = sessionKey.getEncoded();
        byte[] encryptedKey = publicKeyCipherUtil.encrypt(encodedKey, serverPublicKey);
        EncryptedMessage<byte[]> encryptedMessage = new EncryptedMessage<byte[]>(encryptedRequest, encryptedKey);

        byte[] encodedResponse = secureRequestHandler.handleRequest(SerializationUtils.serialize(encryptedMessage));
        byte[] decryptedResponse = secretKeyCipherUtil.decrypt(encodedResponse, sessionKey);

        SecureResponse secureResponse = (SecureResponse) SerializationUtils.deserialize(decryptedResponse);
        secureResponse.verify();
        MethodResult mr = secureResponse.getMessage();
        assertThat((Long) mr.getArg(), is(new Long(43)));
    }

    @Test
    public void testInvalidAuthentication() throws Exception {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "password");
        when(authManager.authenticate(token)).thenThrow(new BadCredentialsException("bad"));
        MethodCall request = new MethodCall("doSomething", new Object[] { "42", }, new HashMap<String, String>());
        SecureRequest secureRequest = SecureRequest.create(request, token);

        SecretKey sessionKey = secretKeyUtil.generateKey(128);

        byte[] serializedRequest = SerializationUtils.serialize(secureRequest);
        byte[] encryptedRequest = secretKeyCipherUtil.encrypt(serializedRequest, sessionKey);

        byte[] encodedKey = sessionKey.getEncoded();
        byte[] encryptedKey = publicKeyCipherUtil.encrypt(encodedKey, serverPublicKey);
        EncryptedMessage<byte[]> encryptedMessage = new EncryptedMessage<byte[]>(encryptedRequest, encryptedKey);

        try {
            secureRequestHandler.handleRequest(SerializationUtils.serialize(encryptedMessage));
        } catch (Exception e) {
            assertThat(e, is(BadCredentialsException.class));
        }

        verify(realHandler, never()).handleCall(any(MethodCall.class));
    }

    @Test(expected = MessageVerificationFailedException.class)
    public void testManipulateMessage() throws Exception {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "password");
        MethodCall request = new MethodCall("doSomething", new Object[]{ "42", }, new HashMap<String, String>());
        SecureRequest secureRequest = SecureRequest.create(request, token);

        SecretKey sessionKey = secretKeyUtil.generateKey(128);

        request.setArgs(new Object[]{ "43" }); // manipulate message

        byte[] serializedRequest = SerializationUtils.serialize(secureRequest);
        byte[] encryptedRequest = secretKeyCipherUtil.encrypt(serializedRequest, sessionKey);

        byte[] encodedKey = sessionKey.getEncoded();
        byte[] encryptedKey = publicKeyCipherUtil.encrypt(encodedKey, serverPublicKey);

        EncryptedMessage<byte[]> encryptedMessage = new EncryptedMessage<byte[]>(encryptedRequest, encryptedKey);

        secureRequestHandler.handleRequest(SerializationUtils.serialize(encryptedMessage));
    }

    private void setupRequestHandler() {
        BinaryMessageCryptUtil cryptUtil = new BinaryMessageCryptUtil();
        secureRequestHandler.setCryptUtil(cryptUtil);
        secureRequestHandler.setPrivateKey(serverPrivateKey);

        authManager = mock(AuthenticationManager.class);
        secureRequestHandler.setAuthManager(authManager);

        realHandler = mock(RequestHandler.class);
        MethodResult ref = new MethodResult(new Long(43L));
        when(realHandler.handleCall(any(MethodCall.class))).thenReturn(ref);
        secureRequestHandler.setRealHandler(realHandler);
    }
}
