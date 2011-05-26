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

package org.openengsb.core.common.security.filter;

import java.util.Map;

import javax.crypto.SecretKey;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.DecryptionException;
import org.openengsb.core.api.security.EncryptionException;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.openengsb.core.common.security.CipherUtils;
import org.openengsb.core.common.security.PrivateKeySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageCryptoFilter extends AbstractFilterChainElement<EncryptedMessage, byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCryptoFilter.class);

    private FilterAction next;

    private PrivateKeySource privateKeySource;
    private String secretKeyAlgorithm;

    public MessageCryptoFilter(PrivateKeySource privateKeySource, String secretKeyAlgorithm) {
        super(EncryptedMessage.class, byte[].class);
        this.privateKeySource = privateKeySource;
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    @Override
    protected byte[] doFilter(EncryptedMessage input, Map<String, Object> metaData) {
        byte[] encryptedKey = input.getEncryptedKey();
        byte[] decryptedMessage;
        SecretKey sessionKey;
        LOGGER.debug("decrypting encryptedMessage");
        try {
            LOGGER.trace("decrypting session-key");
            byte[] sessionKeyData = CipherUtils.decrypt(encryptedKey, privateKeySource.getPrivateKey());
            sessionKey = CipherUtils.deserializeSecretKey(sessionKeyData, secretKeyAlgorithm);
            LOGGER.trace("decrypting message using session-key");
            decryptedMessage = CipherUtils.decrypt(input.getEncryptedContent(), sessionKey);
        } catch (DecryptionException e) {
            throw new FilterException(e);
        }
        LOGGER.debug("forwarding decrypted message to next filter {}", next);
        byte[] plainResult = (byte[]) next.filter(decryptedMessage, metaData);
        try {
            LOGGER.trace("encrypting result using previously decrypted session-key");
            return CipherUtils.encrypt(plainResult, sessionKey);
        } catch (EncryptionException e) {
            throw new FilterException(e);
        }
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, byte[].class, byte[].class);
        this.next = next;
    }

}
