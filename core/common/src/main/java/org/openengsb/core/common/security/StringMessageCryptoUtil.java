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
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.openengsb.core.api.security.model.DecryptionException;
import org.openengsb.core.api.security.model.EncryptionException;

public class StringMessageCryptoUtil extends AbstractMessageCryptUtil<String> {
    @Override
    public String decrypt(String encContent, SecretKey sessionKey) throws DecryptionException {
        byte[] content = Base64.decodeBase64(encContent);
        byte[] decryptedData = cipherUtil.decrypt(content, sessionKey);
        return StringUtils.newStringUtf8(decryptedData);
    }

    @Override
    public SecretKey decryptKey(String encryptedKey, PrivateKey key) throws DecryptionException {
        byte[] data = Base64.decodeBase64(encryptedKey);
        byte[] keyData = cipherUtil.decrypt(data, key);
        return keySerializer.deserializeSecretKey(keyData);
    }

    @Override
    public String encrypt(String content, SecretKey sessionKey) throws EncryptionException {
        byte[] data = StringUtils.getBytesUtf8(content);
        byte[] encryptData = cipherUtil.encrypt(data, sessionKey);
        return Base64.encodeBase64String(encryptData);
    }

    @Override
    public String encryptKey(SecretKey sessionKey, PublicKey serverPublicKey) throws EncryptionException {
        byte[] encodedKey = sessionKey.getEncoded();
        byte[] encryptedData = cipherUtil.encrypt(encodedKey, serverPublicKey);
        return Base64.encodeBase64String(encryptedData);
    }

}
