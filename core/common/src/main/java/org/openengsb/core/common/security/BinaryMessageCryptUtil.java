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

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.openengsb.core.api.security.model.DecryptionException;
import org.openengsb.core.api.security.model.EncryptionException;

public class BinaryMessageCryptUtil extends AbstractMessageCryptUtil<byte[]> {

    public BinaryMessageCryptUtil() {
    }

    public BinaryMessageCryptUtil(AlgorithmConfig config) {
        super(config);
    }

    @Override
    public byte[] decrypt(byte[] encContent, SecretKey sessionKey) throws DecryptionException {
        return cipherUtil.decrypt(encContent, sessionKey);
    }

    @Override
    public SecretKey decryptKey(byte[] encryptedKey, PrivateKey privateKey) throws DecryptionException {
        byte[] encodedSessionKey = cipherUtil.decrypt(encryptedKey, privateKey);
        return keySerializer.deserializeSecretKey(encodedSessionKey);
    }

    @Override
    public byte[] encrypt(byte[] content, SecretKey sessionKey) throws EncryptionException {
        return cipherUtil.encrypt(content, sessionKey);
    }
}
