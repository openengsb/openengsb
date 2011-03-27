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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class PublicKeyUtil {

    private KeyPairGenerator keyGenerator;
    private KeyFactory keyFactory;

    public PublicKeyUtil() {
        this("RSA");
    }

    public PublicKeyUtil(String algorithm) {
        try {
            keyGenerator = KeyPairGenerator.getInstance(algorithm);
            keyFactory = KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public KeyPair generateKey(int keySize) throws NoSuchAlgorithmException {
        keyGenerator.initialize(keySize);
        KeyPair key = keyGenerator.generateKeyPair();
        return key;
    }

    public String serializeKey(Key key) {
        return new String(Base64.encodeBase64(key.getEncoded()));
    }

    public PublicKey deserializePublicKey(String keyString) throws InvalidKeyException,
        NoSuchAlgorithmException,
            InvalidKeySpecException {
        byte[] pubKeyBytes = Base64.decodeBase64(keyString.getBytes());
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubKeyBytes);
        return keyFactory.generatePublic(pubSpec);
    }

    public PrivateKey deserializePrivateKey(String keyString) throws InvalidKeyException, KeyStoreException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privKeyBytes = Base64.decodeBase64(keyString.getBytes());
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
        return keyFactory.generatePrivate(privSpec);
    }

}
