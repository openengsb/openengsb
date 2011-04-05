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
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SecretKeyUtil {

    private KeyGenerator keyGenerator;
    private String algorithm;
    private int keySize;

    public SecretKeyUtil() {
        this("AES", 128);
    }

    public SecretKeyUtil(String algorithm, int keySize) {
        try {
            keyGenerator = KeyGenerator.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Generate key which contains a pair of private and public key using 1024 bytes
     *
     * @return key pair
     * @throws NoSuchAlgorithmException
     */
    public SecretKey generateKey() {
        keyGenerator.init(keySize);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public String serializeKey(Key key) {
        return new String(Base64.encodeBase64(key.getEncoded()));
    }

    public SecretKey deserializeKey(String keyString) {
        return new SecretKeySpec(Base64.decodeBase64(keyString), algorithm);
    }

    public SecretKey deserializeKey(byte[] rawKey) {
        return new SecretKeySpec(Base64.decodeBase64(rawKey), algorithm);
    }

}
