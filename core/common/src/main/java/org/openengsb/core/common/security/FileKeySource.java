package org.openengsb.core.common.security;

import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.FileUtils;

public class FileKeySource implements PrivateKeySource {

    private File privateKeyFile;
    private KeyFactory keyFactory;

    private long keyFileModificationTime;
    private PrivateKey privateKey;

    public FileKeySource() {
    }

    public FileKeySource(File keyFile, String algorithm) {
        this.privateKeyFile = keyFile;
        setAlgorithm(algorithm);
        update();
    }

    public void setKeyFilename(String keyFile) {
        File file = new File(keyFile);
        if (file.isAbsolute()) {
            this.privateKeyFile = file;
        } else {
            this.privateKeyFile = new File(System.getProperty("karaf.base"), keyFile);
        }
    }

    public void setAlgorithm(String algorithm) {
        try {
            keyFactory = KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void update() {
        long lastModified = this.privateKeyFile.lastModified();
        if (lastModified == this.keyFileModificationTime) {
            return;
        }
        this.keyFileModificationTime = lastModified;
        byte[] keyData;
        try {
            keyData = FileUtils.readFileToByteArray(privateKeyFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        KeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
        try {
            this.privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public PrivateKey getPrivateKey() {
        update();
        return privateKey;
    }
}
