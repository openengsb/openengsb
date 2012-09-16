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

package org.openengsb.core.util;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.openengsb.core.api.security.DecryptionException;
import org.openengsb.core.api.security.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CipherUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CipherUtils.class);

    public static final String DEFAULT_SIGN_ALGORITHM = "SHA1withRSA";
    public static final String DEFAULT_SYMMETRIC_ALGORITHM = "AES";
    public static final int DEFAULT_SYMMETRIC_KEYSIZE = 128;
    public static final String DEFAULT_ASYMMETRIC_ALGORITHM = "RSA";
    public static final int DEFAULT_ASYMMETRIC_KEYSIZE = 2048;

    /**
     * Decrypts the given data using the given key. The key holds the algorithm used for decryption. If you are
     * decrypting data that is supposed to be a string, consider that it might be Base64-encoded.
     *
     * @throws DecryptionException if the string cannot be decrypted with the given key
     */
    public static byte[] decrypt(byte[] text, Key key) throws DecryptionException {
        return decrypt(text, key, key.getAlgorithm());
    }

    /**
     * Decrypts the given data using the given key using the given algorithm. If you are decrypting data that is
     * supposed to be a string, consider that it might be Base64-encoded.
     *
     * @throws DecryptionException if the string cannot be decrypted with the given key
     * @throws IllegalArgumentException if the algorithm is not supported.
     */
    public static byte[] decrypt(byte[] text, Key key, String algorithm) throws DecryptionException {
        Cipher cipher;
        try {
            LOGGER.trace("start decrypting text using {} cipher", algorithm);
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            LOGGER.trace("initialized decryption with key of type {}", key.getClass());
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("unable to initialize cipher for algorithm " + algorithm, e);
        }
        try {
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            throw new DecryptionException("unable to decrypt data using algorithm " + algorithm, e);
        }
    }

    /**
     * Encrypts the given data using the given key. The key holds the algorithm used for encryption. If you are
     * encrypting data that is supposed to be a string, consider that it might be Base64-encoded.
     *
     * @throws EncryptionException if the string cannot be encrypted with the given key
     */
    public static byte[] encrypt(byte[] text, Key key) throws EncryptionException {
        return encrypt(text, key, key.getAlgorithm());
    }

    /**
     * Encrypts the given data using the given key using the given algorithm. If you are encrypting data that is
     * supposed to be a string, consider encoding it in Base64.
     *
     * @throws EncryptionException if the string cannot be encrypted with the given key
     * @throws IllegalArgumentException if the algorithm is not supported.
     */
    public static byte[] encrypt(byte[] text, Key key, String algorithm) throws EncryptionException {
        Cipher cipher;
        try {
            LOGGER.trace("start encrypting text using {} cipher", algorithm);
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            LOGGER.trace("initialized encryption with key of type {}", key.getClass());
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("unable to initialize cipher for algorithm " + algorithm, e);
        }
        try {
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("unable to encrypt data using algorithm " + algorithm, e);
        }
    }

    /**
     * converts a byte[] that originally was created using {@link PublicKey#getEncoded()} back to the corresponding
     * instance.
     *
     * Example: CipherUtils.deserializePublicKey(data, "RSA")
     */
    public static PublicKey deserializePublicKey(byte[] keyData, String algorithm) {
        LOGGER.trace("deserialize public key from data using algorithm \"{}\"", algorithm);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyData);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return keyFactory.generatePublic(pubSpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("provided data could not be converted to a PublicKey for algorithm "
                    + algorithm, e);
        }
    }

    /**
     * converts a byte[] that originally was created using {@link PrivateKey#getEncoded()} back to the corresponding
     * instance.
     *
     * Example: CipherUtils.deserializePrivateKey(data, "RSA")
     */
    public static PrivateKey deserializePrivateKey(byte[] keyData, String algorithm) {
        LOGGER.trace("deserialize private key from data using algorithm \"{}\"", algorithm);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(keyData);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return keyFactory.generatePrivate(privSpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("provided data could not be converted to a PrivateKey for algorithm "
                    + algorithm, e);
        }
    }

    /**
     * converts a byte[] that originally was created using {@link SecretKey#getEncoded()} back to the corresponding
     * instance.
     *
     * Example: CipherUtils.deserializeSecretKey(data, "AES")
     */
    public static SecretKey deserializeSecretKey(byte[] keyData, String algorithm) {
        LOGGER.trace("deserialize secret key from data using algorithm \"{}\"", algorithm);
        return new SecretKeySpec(keyData, algorithm);
    }

    /**
     * Generate a {@link KeyPair} for the given assymmetric algorithm and keysize
     *
     * Example: CipherUtils.generateKeyPair("RSA", 2048)
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize) {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Generate a {@link SecretKey} for the given symmetric algorithm and keysize
     *
     * Example: CipherUtils.generateKeyPair("AES", 128)
     */
    public static SecretKey generateKey(String algorithm, int keySize) {
        KeyGenerator secretKeyGenerator;
        try {
            secretKeyGenerator = KeyGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        secretKeyGenerator.init(keySize);
        return secretKeyGenerator.generateKey();
    }

    /**
     * create a signature for the given text using the PrivateKey and algorithm
     *
     * Example: CipherUtils.sign(data, key, CipherUtils.DEFAULT_SIGN_ALGORITHM)
     *
     * @throws SignatureException if the data cannot not be signed
     */
    public static byte[] sign(byte[] text, PrivateKey key, String algorithm) throws SignatureException {
        Signature signature;
        try {
            signature = Signature.getInstance(algorithm);
            signature.initSign(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("cannot initialize signature for algorithm " + algorithm, e);
        }
        signature.update(text);
        return signature.sign();
    }

    /**
     * verifies if the given data is valid for the given signature and public key.
     *
     * @throws SignatureException if the algorithm cannot be initialized
     */
    public static boolean verify(byte[] text, byte[] signatureValue, PublicKey key, String algorithm)
        throws SignatureException {
        Signature signature;
        try {
            signature = Signature.getInstance(algorithm);
            signature.initVerify(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("cannot initialize signature for algorithm " + algorithm, e);
        }
        signature.update(text);
        return signature.verify(signatureValue);
    }

    private CipherUtils() {
    }

}
