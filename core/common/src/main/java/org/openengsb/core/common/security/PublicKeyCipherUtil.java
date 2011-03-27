package org.openengsb.core.common.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PublicKeyCipherUtil implements CipherUtil, VerificationUtil {

    private static final String DEFAULT_ENCRYPTION = "RSA";
    private static final String DEFAULT_SIGN = "SHA1withRSA";
    private Cipher cipher;
    private Signature signature;

    public PublicKeyCipherUtil() {
        try {
            init(DEFAULT_ENCRYPTION, DEFAULT_SIGN);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKeyCipherUtil(String encryptionAlgorithm, String signAlgorithm) throws NoSuchAlgorithmException,
        NoSuchPaddingException {
        init(encryptionAlgorithm, signAlgorithm);
    }

    private void init(String encryptionAlgorithm, String signAlgorithm) throws NoSuchAlgorithmException,
        NoSuchPaddingException {
        cipher = Cipher.getInstance(encryptionAlgorithm);
        signature = Signature.getInstance(signAlgorithm);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openengsb.core.common.security.CipherUtil#encrypt(byte[], java.security.PublicKey)
     */
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

    /*
     * (non-Javadoc)
     *
     * @see org.openengsb.core.common.security.CipherUtil#decrypt(byte[], java.security.PrivateKey)
     */
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

    /*
     * (non-Javadoc)
     *
     * @see org.openengsb.core.common.security.CipherUtil#sign(byte[], java.security.PrivateKey)
     */
    @Override
    public byte[] sign(byte[] text, Key key) throws SecurityException {
        try {
            return doSign(text, (PrivateKey) key);
        } catch (SignatureException e) {
            throw new SecurityException(e);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openengsb.core.common.security.CipherUtil#verify(byte[], byte[], java.security.PublicKey)
     */
    @Override
    public boolean verify(byte[] text, byte[] signature, Key key) {
        try {
            return doVerify(text, signature, (PublicKey) key);
        } catch (SignatureException e) {
            throw new SecurityException("verify failed", e);
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

    private byte[] doSign(byte[] text, PrivateKey key) throws SignatureException {
        try {
            signature.initSign(key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
        signature.update(text);
        return signature.sign();
    }

    private boolean doVerify(byte[] text, byte[] signatureValue, PublicKey key) throws SignatureException {
        try {
            signature.initVerify(key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
        signature.update(text);
        return signature.verify(signatureValue);
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

    public void setSignAlgorithm(String signAlgorithm) {
        try {
            signature = Signature.getInstance(signAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
