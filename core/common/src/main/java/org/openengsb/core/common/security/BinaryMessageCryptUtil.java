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

import java.security.Key;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BinaryMessageCryptUtil extends AbstractMessageCryptUtil<byte[]> {

    public BinaryMessageCryptUtil() {
    }

    public BinaryMessageCryptUtil(String publicKeyAlgorithm, String symmetricAlgorithm) {
        super(publicKeyAlgorithm, symmetricAlgorithm);
    }

    @Override
    public byte[] decrypt(byte[] encContent, SecretKey sessionKey) {
        return symCipherUtil.decrypt(encContent, sessionKey);
    }

    @Override
    public SecretKey decryptKey(byte[] encryptedKey, Key privateKey) {
        byte[] encodedSessionKey = pubCipherUtil.decrypt(encryptedKey, privateKey);
        return new SecretKeySpec(encodedSessionKey, this.symmectricAlgorithm);
    }

    @Override
    public byte[] encrypt(byte[] content, SecretKey sessionKey) {
        return symCipherUtil.encrypt(content, sessionKey);
    }

}
