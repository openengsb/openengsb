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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyGeneratorUtils {

    private KeyPairGenerator keyPairGenerator;
    private int publicKeySize;

    private KeyGenerator secretKeyGenerator;
    private int secretKeySize;

    public KeyGeneratorUtils() {
    }

    public KeyGeneratorUtils(AlgorithmConfig config) {
        setAlgorithmConfig(config);
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        keyPairGenerator.initialize(publicKeySize);
        return keyPairGenerator.generateKeyPair();
    }

    public SecretKey generateKey() {
        secretKeyGenerator.init(secretKeySize);
        return secretKeyGenerator.generateKey();
    }

    public void setPublicKeySize(int publicKeySize) {
        this.publicKeySize = publicKeySize;
    }

    public void setSecretKeySize(int secretKeySize) {
        this.secretKeySize = secretKeySize;
    }

    public void setAlgorithmConfig(AlgorithmConfig config) {
        setPublicKeyAlgorithm(config.getPublicKeyAlgorithm());
        this.publicKeySize = config.getPublicKeySize();
        setSecretKeyAlgorithm(config.getSecretKeyAlgorithm());
        this.secretKeySize = config.getSecretKeySize();
    }

    public void setPublicKeyAlgorithm(String algorithm) {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setSecretKeyAlgorithm(String algorithm) {
        try {
            secretKeyGenerator = KeyGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
