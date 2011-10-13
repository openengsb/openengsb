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

package org.openengsb.core.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.common.util.CipherUtils;
import org.openengsb.core.test.AbstractOpenEngSBTest;

public class CipherUtilTest extends AbstractOpenEngSBTest {

    private static final String DEFAULT_ENCODING = "UTF-8";

    /*
     * for the sake of completeness, maybe we need it sometime
     * 
     * private static final String PUBLIC_KEY_64 = "" +
     * "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEwQedUFElYBNOW71NYLgKEGSqKEbGQ9xhlCjS" +
     * "9qd8A7MdaVub61Npc6wSuLJNK1qnrSufWkiZxuo7IsyFnZl9bqkr1D/x4UqKEBmGZIh4s4WIMymw" +
     * "TGu2HmAKuKO7JypfQpHemZpLmXTsNse1xFhTfshxWJq4+WqBdeoYZ8p1iwIDAQAB";
     */

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

    private PublicKey generatedPublickey;
    private PrivateKey generatedPrivatekey;

    @Before
    public void setUp() throws Exception {
        KeyPair kp = CipherUtils.generateKeyPair("RSA", 2048);
        generatedPublickey = kp.getPublic();
        generatedPrivatekey = kp.getPrivate();
    }

    @Test
    public void encryptSymmetricKeyWithPublicKey_shouldBeTheSameWhenDecryptedWithPrivateKey() throws Exception {
        SecretKey secretKey = CipherUtils.generateKey("AES", 128);

        byte[] encoded = secretKey.getEncoded();
        byte[] encryptedKey = CipherUtils.encrypt(encoded, generatedPublickey);

        byte[] decryptKey = CipherUtils.decrypt(encryptedKey, generatedPrivatekey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decryptKey, "AES");

        assertThat(secretKeySpec, is(secretKey));
    }

    @Test
    public void encryptMessageWithGeneratedPublicKey_shouldBeTheSameAfterDecryptionWithPrivateKey() throws Exception {
        byte[] data = TEST_STRING.getBytes(DEFAULT_ENCODING);
        byte[] encrypted = CipherUtils.encrypt(data, generatedPublickey);
        byte[] decrypted = CipherUtils.decrypt(encrypted, generatedPrivatekey);
        String result = new String(decrypted, DEFAULT_ENCODING);
        assertEquals(TEST_STRING, result);
    }

    @Test
    public void readPublicKeyFromByteArray_shouldBeSamePublicKey() throws Exception {
        byte[] data = generatedPublickey.getEncoded();
        PublicKey parsedKey = CipherUtils.deserializePublicKey(data, "RSA");
        assertEquals(generatedPublickey, parsedKey);
    }

    @Test
    public void readPrivateKeyFromByteArray_shouldBeSamePrivateKey() throws Exception {
        byte[] data = generatedPrivatekey.getEncoded();
        PrivateKey parsedKey = CipherUtils.deserializePrivateKey(data, "RSA");
        assertEquals(generatedPrivatekey, parsedKey);
    }

    @Test
    public void decryptUsingPrivateKey_shouldResultInDecryptedText() throws Exception {
        PrivateKey key = CipherUtils.deserializePrivateKey(Base64.decodeBase64(PRIVATE_KEY_64), "RSA");
        byte[] data = CipherUtils.decrypt(Base64.decodeBase64(TEST_STRING_CIPHERED), key);
        String testString = new String(data);
        assertEquals(TEST_STRING, testString);
    }

    @Test
    public void signAndVerifyMessage_shouldVerifyOK() throws Exception {
        byte[] data = TEST_STRING.getBytes(DEFAULT_ENCODING);
        byte[] signature = CipherUtils.sign(data, generatedPrivatekey, CipherUtils.DEFAULT_SIGN_ALGORITHM);
        assertTrue(CipherUtils.verify(data, signature, generatedPublickey, CipherUtils.DEFAULT_SIGN_ALGORITHM));
    }

    @Test
    public void signAndVerifyAndManipulate_shouldCauseVerificationFailure() throws Exception {
        byte[] data = TEST_STRING.getBytes(DEFAULT_ENCODING);
        byte[] signature = CipherUtils.sign(data, generatedPrivatekey, CipherUtils.DEFAULT_SIGN_ALGORITHM);
        data[0]++;
        assertFalse(CipherUtils.verify(data, signature, generatedPublickey, CipherUtils.DEFAULT_SIGN_ALGORITHM));
    }

    @Test
    public void encryptSymmetric() throws Exception {
        SecretKey generateKey = CipherUtils.generateKey("AES", 128);

        byte[] encrypt = CipherUtils.encrypt(TEST_STRING.getBytes(), generateKey);
        byte[] decrypt = CipherUtils.decrypt(encrypt, generateKey);

        assertThat(new String(decrypt), is(TEST_STRING));
    }

    @Test
    public void encryptBlowfish() throws Exception {
        SecretKey generateKey = CipherUtils.generateKey("Blowfish", 128);

        byte[] encrypt = CipherUtils.encrypt(TEST_STRING.getBytes(), generateKey);
        byte[] decrypt = CipherUtils.decrypt(encrypt, generateKey);

        assertThat(new String(decrypt), is(TEST_STRING));
    }

}
