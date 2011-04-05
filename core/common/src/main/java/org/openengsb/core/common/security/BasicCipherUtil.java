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

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.openengsb.core.api.security.model.DecryptionException;
import org.openengsb.core.api.security.model.EncryptionException;

public class BasicCipherUtil {

    private Cipher publicKeyCipher;
    private Cipher secretKeyCipher;

    public BasicCipherUtil() {
    }

    public BasicCipherUtil(String publicKeyAlgorithm, String secretKeyAlgorithm) {
        setPublicKeyAlgorithm(publicKeyAlgorithm);
        setSecretKeyAlgorithm(secretKeyAlgorithm);
    }

    public BasicCipherUtil(AlgorithmConfig config) {
        setAlgorithmConfig(config);
    }

    public void setAlgorithmConfig(AlgorithmConfig config) {
        setPublicKeyAlgorithm(config.getPublicKeyAlgorithm());
        setSecretKeyAlgorithm(config.getSecretKeyAlgorithm());
    }

    public synchronized byte[] encrypt(byte[] text, PublicKey key) throws EncryptionException {
        return doEncrypt(publicKeyCipher, key, text);
    }

    public synchronized byte[] decrypt(byte[] text, PrivateKey key) throws DecryptionException {
        return doDecrypt(publicKeyCipher, key, text);
    }

    public synchronized void setPublicKeyAlgorithm(String algorithm) {
        try {
            publicKeyCipher = Cipher.getInstance(algorithm);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public synchronized byte[] encrypt(byte[] text, SecretKey key) throws EncryptionException {
        return doEncrypt(secretKeyCipher, key, text);
    }

    public synchronized byte[] decrypt(byte[] text, SecretKey key) throws DecryptionException {
        return doDecrypt(secretKeyCipher, key, text);
    }

    public synchronized void setSecretKeyAlgorithm(String algorithm) {
        try {
            secretKeyCipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private byte[] doDecrypt(Cipher cipher, Key key, byte[] text) throws DecryptionException {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
        try {
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            throw new DecryptionException(e);
        }
    }

    private byte[] doEncrypt(Cipher cipher, Key key, byte[] text) throws EncryptionException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
        try {
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException(e);
        }
    }

}
