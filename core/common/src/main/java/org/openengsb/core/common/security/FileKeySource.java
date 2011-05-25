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

import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.io.FileUtils;

public class FileKeySource implements PrivateKeySource, PublicKeySource {

    private static final String DEFAULT_PRIVATE_KEY_FILENAME = "private.key.data";
    private static final String DEFAULT_PUBLIC_KEY_FILENAME = "public.key.data";

    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    private String algorithm;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public FileKeySource() {
    }

    public FileKeySource(String keyDirectory, String algorithm) {
        setAlgorithm(algorithm);
        setKeyDirectory(keyDirectory);
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    private KeyFactory getKeyFactory() {
        try {
            return KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setKeyDirectory(String keyDirectory) {
        File keyDirectoryFile = new File(keyDirectory);
        if (!keyDirectoryFile.isAbsolute()) {
            keyDirectoryFile = new File(System.getProperty("karaf.home"), keyDirectory);
        }
        if (!keyDirectoryFile.exists()) {
            keyDirectoryFile.mkdirs();
        } else if (!keyDirectoryFile.isDirectory()) {
            throw new IllegalArgumentException("keydir must be a directory");
        }
        generateKeysIfRequired(keyDirectoryFile);
        privateKey = readPrivateKeyFromFile(keyDirectoryFile);
        publicKey = readPublicKeyFromFile(keyDirectoryFile);
    }

    private void generateKeysIfRequired(File keyDirectoryFile) {
        File privateKeyFile = new File(keyDirectoryFile, DEFAULT_PRIVATE_KEY_FILENAME);
        if (privateKeyFile.exists()) {
            return;
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
            generator.initialize(DEFAULT_KEY_SIZE);
            KeyPair generatedKeyPair = generator.generateKeyPair();
            FileUtils.writeByteArrayToFile(privateKeyFile, generatedKeyPair.getPrivate().getEncoded());
            File publicKeyFile = new File(keyDirectoryFile, DEFAULT_PUBLIC_KEY_FILENAME);
            FileUtils.writeByteArrayToFile(publicKeyFile, generatedKeyPair.getPublic().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected PrivateKey readPrivateKeyFromFile(File keyDirectory) {
        byte[] keyData;
        try {
            keyData = FileUtils.readFileToByteArray(new File(keyDirectory, DEFAULT_PRIVATE_KEY_FILENAME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        KeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
        try {
            return getKeyFactory().generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    protected PublicKey readPublicKeyFromFile(File keyDirectory) {
        byte[] keyData;
        try {
            keyData = FileUtils.readFileToByteArray(new File(keyDirectory, DEFAULT_PUBLIC_KEY_FILENAME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        KeySpec keySpec = new X509EncodedKeySpec(keyData);
        try {
            return getKeyFactory().generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
