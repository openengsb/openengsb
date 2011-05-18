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

    private KeyFactory keyFactory;

    private FileSource<PrivateKey> privateKeySource;
    private FileSource<PublicKey> publicKeySource;

    private final class PrivateKeyFileSource extends FileSource<PrivateKey> {
        public PrivateKeyFileSource(File file) {
            super(file);
        }

        @Override
        protected PrivateKey updateValue(File file) {
            byte[] keyData;
            try {
                keyData = FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            KeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
            try {
                return keyFactory.generatePrivate(keySpec);
            } catch (InvalidKeySpecException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    private final class PublicKeyFileSource extends FileSource<PublicKey> {
        public PublicKeyFileSource(File file) {
            super(file);
        }

        @Override
        protected PublicKey updateValue(File file) {
            byte[] keyData;
            try {
                keyData = FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            KeySpec keySpec = new X509EncodedKeySpec(keyData);
            try {
                return keyFactory.generatePublic(keySpec);
            } catch (InvalidKeySpecException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    public FileKeySource() {
    }

    public FileKeySource(String keyDirectory, String algorithm) {
        setKeyDirectory(keyDirectory);
        setAlgorithm(algorithm);
    }

    public void setAlgorithm(String algorithm) {
        try {
            keyFactory = KeyFactory.getInstance(algorithm);
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
        privateKeySource = new PrivateKeyFileSource(new File(keyDirectoryFile, DEFAULT_PRIVATE_KEY_FILENAME));
        publicKeySource = new PublicKeyFileSource(new File(keyDirectoryFile, DEFAULT_PUBLIC_KEY_FILENAME));
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

    @Override
    public PrivateKey getPrivateKey() {
        return privateKeySource.getValue();
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKeySource.getValue();
    }
}
