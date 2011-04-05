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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeySerializationUtil {

    private KeyFactory publicKeyFactory;
    private String secretKeyAlgorithm;

    public KeySerializationUtil() {
    }

    public KeySerializationUtil(String publicKeyAlgorithm, String secretKeyAlgorithm) {
        setPublicKeyAlgorithm(publicKeyAlgorithm);
        setSecretKeyAlgorithm(secretKeyAlgorithm);
    }

    public KeySerializationUtil(AlgorithmConfig config) {
        setAlgorithmConfig(config);
    }

    public void setAlgorithmConfig(AlgorithmConfig config) {
        setPublicKeyAlgorithm(config.getPublicKeyAlgorithm());
        setSecretKeyAlgorithm(config.getSecretKeyAlgorithm());
    }

    private void setSecretKeyAlgorithm(String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    private void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        try {
            publicKeyFactory = KeyFactory.getInstance(publicKeyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public PublicKey deserializePublicKey(byte[] keyData) {
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyData);
        try {
            return publicKeyFactory.generatePublic(pubSpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public PrivateKey deserializePrivateKey(byte[] keyData) {
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(keyData);
        try {
            return publicKeyFactory.generatePrivate(privSpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public SecretKey deserializeSecretKey(byte[] keyData) {
        return new SecretKeySpec(keyData, secretKeyAlgorithm);
    }

}
