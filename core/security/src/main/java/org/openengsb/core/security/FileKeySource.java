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

package org.openengsb.core.security;

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
import org.openengsb.core.api.security.PrivateKeySource;
import org.openengsb.core.api.security.PublicKeySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * public and private keys form binary files in a specific directory. The files are expected to have the names:
 * {public,private}.key.data
 *
 * If the configured path is not absolute it is assumed to be a subdirectory of ${karaf.data}
 *
 * Keys are only read once and then kept in memory, so it does not act like a deployer.
 *
 */
public class FileKeySource implements PrivateKeySource, PublicKeySource {

    private static final String DEFAULT_KEY_DIR = "etc/keys";

    private static final Logger LOGGER = LoggerFactory.getLogger(FileKeySource.class);

    private static final String DEFAULT_PRIVATE_KEY_FILENAME = "private.key.data";
    private static final String DEFAULT_PUBLIC_KEY_FILENAME = "public.key.data";

    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    private String algorithm;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private String keyDirectory;

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
        this.keyDirectory = keyDirectory;
    }

    private File getKeyDirectoryFile() {
        if (keyDirectory == null) {
            LOGGER.info("no key-directory defined, defaulting to {}", DEFAULT_KEY_DIR);
            keyDirectory = DEFAULT_KEY_DIR;
        }
        LOGGER.debug("using {} as keyDirectory", keyDirectory);
        File keyDirectoryFile = new File(keyDirectory);
        if (keyDirectoryFile.isAbsolute()) {
            return keyDirectoryFile;
        }
        LOGGER.info("understanding {} to be a subdirectory of karaf.data", keyDirectory);
        return new File(System.getProperty("karaf.home"), keyDirectory);
    }

    public void init() {
        LOGGER.trace("initialize FileKeySource");
        File keyDirectoryFile = getKeyDirectoryFile();
        makeSureDirectoryExists(keyDirectoryFile);
        generateKeysIfRequired(keyDirectoryFile);
        privateKey = readPrivateKeyFromFile(keyDirectoryFile);
        publicKey = readPublicKeyFromFile(keyDirectoryFile);
    }

    private void makeSureDirectoryExists(File keyDirectoryFile) {
        if (keyDirectoryFile.exists()) {
            Preconditions.checkState(keyDirectoryFile.isDirectory(), "%s is not a directory", keyDirectoryFile);
        } else {
            LOGGER.info("creating keydir: {}", keyDirectoryFile.getAbsolutePath());
            keyDirectoryFile.mkdirs();
        }
    }

    private void generateKeysIfRequired(File keyDirectoryFile) {
        File privateKeyFile = new File(keyDirectoryFile, DEFAULT_PRIVATE_KEY_FILENAME);
        File publicKeyFile = new File(keyDirectoryFile, DEFAULT_PUBLIC_KEY_FILENAME);
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            LOGGER.info("skipping key-generation, because there already are some");
            return;
        }
        KeyPairGenerator generator;
        try {
            LOGGER.info("generating new keypair");
            generator = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("failed to generate keypair", e);
        }
        generator.initialize(DEFAULT_KEY_SIZE);
        KeyPair generatedKeyPair = generator.generateKeyPair();
        try {
            LOGGER.trace("saving new keypair to files");
            FileUtils.writeByteArrayToFile(privateKeyFile, generatedKeyPair.getPrivate().getEncoded());
            FileUtils.writeByteArrayToFile(publicKeyFile, generatedKeyPair.getPublic().getEncoded());
        } catch (IOException e) {
            throw new IllegalStateException("failed to write keys to key-directory", e);
        }
    }

    protected PrivateKey readPrivateKeyFromFile(File keyDirectory) {
        byte[] keyData;
        try {
            File file = new File(keyDirectory, DEFAULT_PRIVATE_KEY_FILENAME);
            LOGGER.trace("reading private key form file: {}", file.getAbsolutePath());
            keyData = FileUtils.readFileToByteArray(file);
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
            File file = new File(keyDirectory, DEFAULT_PUBLIC_KEY_FILENAME);
            LOGGER.trace("reading private key form file: {}", file.getAbsolutePath());
            keyData = FileUtils.readFileToByteArray(file);
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
