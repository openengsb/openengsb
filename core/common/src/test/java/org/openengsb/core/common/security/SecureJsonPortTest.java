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

import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import javax.crypto.SecretKey;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.security.filter.EncryptedJsonMessageMarshaller;
import org.openengsb.core.common.security.filter.JsonSecureRequestMarshallerFilter;
import org.openengsb.core.common.security.filter.MessageAuthenticatorFactory;
import org.openengsb.core.common.security.filter.MessageCryptoFilterFactory;
import org.openengsb.core.common.security.filter.MessageVerifierFilter;
import org.openengsb.core.common.security.filter.WrapperFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureJsonPortTest extends GenericSecurePortTest<byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureJsonPortTest.class);

    private ObjectMapper mapper = new ObjectMapper();
    private MessageCryptoUtil<byte[]> cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());

    @Override
    protected SecureResponse decryptAndDecode(byte[] message, SecretKey sessionKey) throws Exception {
        LOGGER.info("decrypting: " + new String(message));
        byte[] decrypt = cryptoUtil.decrypt(message, sessionKey);
        LOGGER.info("decoding: " + new String(decrypt));
        return mapper.readValue(decrypt, SecureResponse.class);
    }

    @Override
    protected byte[] encodeAndEncrypt(SecureRequest secureRequest, SecretKey sessionKey) throws Exception {
        byte[] content = mapper.writeValueAsBytes(secureRequest);
        LOGGER.info("encrypting: " + new String(content));
        byte[] encryptedContent = cryptoUtil.encrypt(content, sessionKey);

        EncryptedMessage encryptedMessage = new EncryptedMessage();
        encryptedMessage.setEncryptedContent(encryptedContent);
        byte[] encryptedKey = cryptoUtil.encryptKey(sessionKey, serverPublicKey);
        encryptedMessage.setEncryptedKey(encryptedKey);
        return mapper.writeValueAsBytes(encryptedMessage);
    }

    @Override
    protected byte[] manipulateMessage(byte[] encryptedRequest) {
        int pos = encryptedRequest.length - (encryptedRequest.length / 8);
        encryptedRequest[pos] -= 1;
        return encryptedRequest;
    }

    @Override
    protected FilterAction getSecureRequestHandlerFilterChain() {
        FilterChainFactory<byte[], byte[]> factory = new FilterChainFactory<byte[], byte[]>(byte[].class, byte[].class);
        ArrayList<Object> filters = new ArrayList<Object>();
        filters.add(EncryptedJsonMessageMarshaller.class);

        FilterChainElementFactory decrypterFactory = new MessageCryptoFilterFactory(serverPrivateKey);
        filters.add(decrypterFactory);

        filters.add(JsonSecureRequestMarshallerFilter.class);
        filters.add(MessageVerifierFilter.class);

        MessageAuthenticatorFactory messageAuthenticatorFactory = new MessageAuthenticatorFactory();
        messageAuthenticatorFactory.setAuthenticationManager(authManager);
        filters.add(messageAuthenticatorFactory);
        filters.add(WrapperFilter.class);

        FilterAction handler = mock(FilterAction.class);
        filters.add(handler);

        factory.setFilters(filters);
        return factory.create();
    }
}
