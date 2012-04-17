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

import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.JsonObjectSerializer;
import org.openengsb.core.common.remote.FilterChain;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.util.CipherUtils;
import org.openengsb.core.security.filter.EncryptedJsonMessageMarshaller;
import org.openengsb.core.security.filter.JsonSecureRequestMarshallerFilterFactory;
import org.openengsb.core.security.filter.MessageCryptoFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureJsonPortTest extends GenericSecurePortTest<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureJsonPortTest.class);
    private JsonObjectSerializer jsonObjectSerializer;
    @Override
    protected SecureResponse decryptAndDecode(String message, SecretKey sessionKey) throws Exception {
        LOGGER.info("decrypting: " + new String(message));
        byte[] decrypt = CipherUtils.decrypt(Base64.decodeBase64(message), sessionKey);
        LOGGER.info("decoding: " + new String(decrypt));
        return jsonObjectSerializer.parse(decrypt, SecureResponse.class);
    }

    @Override
    protected String encodeAndEncrypt(SecureRequest secureRequest, SecretKey sessionKey) throws Exception {
        byte[] content = jsonObjectSerializer.serializeToByteArray(secureRequest);
        LOGGER.info("encrypting: " + new String(content));
        byte[] encryptedContent = CipherUtils.encrypt(content, sessionKey);

        EncryptedMessage encryptedMessage = new EncryptedMessage();
        encryptedMessage.setEncryptedContent(encryptedContent);
        byte[] encryptedKey = CipherUtils.encrypt(sessionKey.getEncoded(), serverPublicKey);
        encryptedMessage.setEncryptedKey(encryptedKey);
        return jsonObjectSerializer.serializeToString(encryptedMessage);
    }

    @Override
    protected String manipulateMessage(String encryptedRequest) {
        return encryptedRequest.replaceAll("a", "b");
    }

    @Override
    protected FilterChain getSecureRequestHandlerFilterChain() {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);

        jsonObjectSerializer = new JsonObjectSerializer();
        jsonObjectSerializer.setBundleContext(bundleContext);
        jsonObjectSerializer.init();
        
        List<Object> asList =
            Arrays.asList(
                EncryptedJsonMessageMarshaller.class,
                new MessageCryptoFilterFactory(privateKeySource, "AES"),
                new JsonSecureRequestMarshallerFilterFactory(jsonObjectSerializer),
                filterTop.create());
        factory.setFilters(asList);
        return factory.create();
    }
}
