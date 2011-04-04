package org.openengsb.core.common.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PublicKeyCipherUtil implements CipherUtil {

    private static final String DEFAULT_ENCRYPTION = "RSA";
    private Cipher cipher;

    public PublicKeyCipherUtil() {
        try {
            init(DEFAULT_ENCRYPTION);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKeyCipherUtil(String encryptionAlgorithm) throws NoSuchAlgorithmException,
        NoSuchPaddingException {
        init(encryptionAlgorithm);
    }

    private void init(String encryptionAlgorithm) throws NoSuchAlgorithmException,
        NoSuchPaddingException {
        cipher = Cipher.getInstance(encryptionAlgorithm);
    }

    @Override
    public byte[] encrypt(byte[] text, Key key) throws SecurityException {
        try {
            return doEncrypt(text, (PublicKey) key);
        } catch (IllegalBlockSizeException e) {
            throw new SecurityException("encrypt failed", e);
        } catch (BadPaddingException e) {
            throw new SecurityException("encrypt failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] text, Key key) throws SecurityException {
        try {
            return doDecrypt(text, (PrivateKey) key);
        } catch (IllegalBlockSizeException e) {
            throw new SecurityException("decrypt failed", e);
        } catch (BadPaddingException e) {
            throw new SecurityException("decrypt failed", e);
        }
    }

    private byte[] doEncrypt(byte[] text, PublicKey key) throws IllegalBlockSizeException, BadPaddingException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
        return cipher.doFinal(text);
    }

    private byte[] doDecrypt(byte[] text, PrivateKey key) throws IllegalBlockSizeException, BadPaddingException {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
        return cipher.doFinal(text);
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        try {
            cipher = Cipher.getInstance(encryptionAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
