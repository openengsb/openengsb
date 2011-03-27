package org.openengsb.core.common.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CipherUtilTest {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String PUBLIC_KEY_64 = ""
            + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEwQedUFElYBNOW71NYLgKEGSqKEbGQ9xhlCjS"
            + "9qd8A7MdaVub61Npc6wSuLJNK1qnrSufWkiZxuo7IsyFnZl9bqkr1D/x4UqKEBmGZIh4s4WIMymw"
            + "TGu2HmAKuKO7JypfQpHemZpLmXTsNse1xFhTfshxWJq4+WqBdeoYZ8p1iwIDAQAB";

    private static final String PRIVATE_KEY_64 = ""
            + "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMTBB51QUSVgE05bvU1guAoQZKoo"
            + "RsZD3GGUKNL2p3wDsx1pW5vrU2lzrBK4sk0rWqetK59aSJnG6jsizIWdmX1uqSvUP/HhSooQGYZk"
            + "iHizhYgzKbBMa7YeYAq4o7snKl9Ckd6ZmkuZdOw2x7XEWFN+yHFYmrj5aoF16hhnynWLAgMBAAEC"
            + "gYEAmyZX+c4e3uke8DhZU04EcjMxHhi6jpdujifF9W147ssAEB5HlfEAinQjaXPpbf7U8soUTwlj"
            + "nJeFlvI+8tIu+J7wuP9m9R/EC02kbYjQUOdmrIXr11GmDNSeKCuklLaQTCKl+eRmVCKk373tmtHE"
            + "/HLAkWsTvdufrkFQi9iaTlECQQDpnHnha5DrcQuUarhwWta+ZDLL56XawfcJZpPfKK2Jgxoqbvg9"
            + "k3i6IRS/kh0g0K98CRK5UvxAiQtDKkDy5z3ZAkEA15xIN5OgfMbE12p83cD4fAU2SpvyzsPk9tTf"
            + "Zb6jnKDAm+hxq1arRyaxL04ppTM/xRRS8DKJLrsAi0HhFzkcAwJAbiuQQyHSX2aZmm3V+46rdXCV"
            + "kBn32rncwf8xP23UoWRFo7tfsNJqfgT53vqOaBpil/FDdkjPk7PNrugvZx5syQJBAJjAEbG+Fu8P"
            + "axkqSjhYpDJJBwOopEa0JhxxB6vveb5XbN2HujAnAMUxtknLWFm/iyg2k+O0Cdhfh60hCTUIsr0C"
            + "QFT8w7k8/FfcAFl+ysJ2lSGpeKkt213QkHpAn2HvHRviVErKSHgEKh10Nf7pU3cgPwHDXNEuQ6Bb"
            + "Ky/vHQD1rMM=";

    private static final String TEST_STRING = "Test";

    private static final String TEST_STRING_CIPHERED = ""
            + "H6Oft0p5m5g28z2XbKdBmaMgIZ/lngwLHGOfpWQ1y7gDOLjWP2RzVTBBkQ/SfhXKdIlFBKU8cslB"
            + "TBg/X355MwSZ1VnK19T0mkRK5HBaBvXrCzadX4no6dxW43rDRbREtTCzwvVv/4duHSJY3d66vX8e"
            + "99baRFRbAfNMyStP7M4=";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private PublicKeyCipherUtil cipherUtil;
    private PublicKey publickey;
    private PrivateKey privatekey;
    private PublicKeyUtil keyUtil;

    @Before
    public void setUp() throws Exception {
        cipherUtil = new PublicKeyCipherUtil();
        keyUtil = new PublicKeyUtil();
        KeyPair kp = keyUtil.generateKey(2048);
        publickey = kp.getPublic();
        privatekey = kp.getPrivate();
    }

    @Test
    public void testGenerate() throws Exception {
        keyUtil.generateKey(2048);
        /*
         * System.out.println("Public key:"); System.out.println("---------------------------------------"); // X509
         * encoded key printByteArray(kp.getPublic().getEncoded());
         * System.out.println("---------------------------------------"); System.out.println("Private key:");
         * System.out.println("---------------------------------------"); // PKCS#8 encoded key
         * printByteArray(kp.getPrivate().getEncoded()); System.out.println("---------------------------------------");
         */
    }

    @Test
    public void testEncryptWithGenerated() throws Exception {
        byte[] data = TEST_STRING.getBytes(DEFAULT_ENCODING);
        byte[] encrypted = cipherUtil.encrypt(data, publickey);
        byte[] decrypted = cipherUtil.decrypt(encrypted, privatekey);
        String result = new String(decrypted, DEFAULT_ENCODING);
        assertEquals(TEST_STRING, result);
    }

    @Test
    public void testReadPublicKey() throws Exception {
        String data = keyUtil.serializeKey(publickey);
        PublicKey parsedKey = keyUtil.deserializePublicKey(data);
        assertEquals(publickey, parsedKey);
    }

    @Test
    public void testReadPrivateKey() throws Exception {
        String data = keyUtil.serializeKey(privatekey);
        PrivateKey parsedKey = keyUtil.deserializePrivateKey(data);
        assertEquals(privatekey, parsedKey);
    }

    @Test
    public void testDecryptReceivedData() throws Exception {
        PrivateKey key = keyUtil.deserializePrivateKey(PRIVATE_KEY_64);
        byte[] data = cipherUtil.decrypt(Base64.decodeBase64(TEST_STRING_CIPHERED), key);
        String testString = new String(data);
        assertEquals(TEST_STRING, testString);
    }

    @Test
    public void testSignAndVerify() throws Exception {
        byte[] data = TEST_STRING.getBytes(DEFAULT_ENCODING);
        byte[] signature = cipherUtil.sign(data, privatekey);
        Assert.assertTrue(cipherUtil.verify(data, signature, publickey));
    }

    @Test
    public void testInvalidSignature() throws Exception {
        byte[] data = TEST_STRING.getBytes(DEFAULT_ENCODING);
        byte[] signature = cipherUtil.sign(data, privatekey);
        data[0] = 0;
        Assert.assertFalse(cipherUtil.verify(data, signature, publickey));
    }

    @Test
    public void encryptSymmetric() throws Exception {
        CipherUtil cipherUtil2 = new SecretKeyCipherUtil();

        SecretKeyUtil secretKeyUtil = new SecretKeyUtil();
        SecretKey generateKey = secretKeyUtil.generateKey(128);

        byte[] encrypt = cipherUtil2.encrypt(TEST_STRING.getBytes(), generateKey);
        byte[] decrypt = cipherUtil2.decrypt(encrypt, generateKey);

        assertThat(new String(decrypt), is(TEST_STRING));
    }

    @Test
    public void encryptBlowfish() throws Exception {
        CipherUtil cipherUtil2 = new SecretKeyCipherUtil("Blowfish");
        SecretKeyUtil secretKeyUtil = new SecretKeyUtil("Blowfish");
        SecretKey generateKey = secretKeyUtil.generateKey(128);

        byte[] encrypt = cipherUtil2.encrypt(TEST_STRING.getBytes(), generateKey);
        byte[] decrypt = cipherUtil2.decrypt(encrypt, generateKey);

        assertThat(new String(decrypt), is(TEST_STRING));
    }

    @Test
    public void encryptWrongKey() throws Exception {
        CipherUtil cipherUtil2 = new SecretKeyCipherUtil("Blowfish");
        SecretKeyUtil secretKeyUtil = new SecretKeyUtil("AES");
        SecretKey generateKey = secretKeyUtil.generateKey(128);

        try {
            cipherUtil2.encrypt(TEST_STRING.getBytes(), generateKey);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getCause(), is(InvalidKeyException.class));
        }

    }
}
