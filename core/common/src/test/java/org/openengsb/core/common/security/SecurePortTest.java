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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.security.model.SecureRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class SecurePortTest {

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

    @Test
    public void testDefaultImpls() throws Exception {
        SecureRequestHandler<byte[]> req = new SecureRequestHandler<byte[]>() {
            @Override
            public SecureRequest unmarshalRequest(byte[] input) {
                return (SecureRequest) SerializationUtils.deserialize(input);
            }
        };
        PrivateKey privateKey = new PublicKeyUtil().deserializePrivateKey(PRIVATE_KEY_64);
        BinaryMessageCipher cipher = new BinaryMessageCipher(new PublicKeyCipherUtil(), privateKey);
        req.setMessageDecrypter(cipher);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "password");

        Map<String, String> emptyMap = Collections.emptyMap();
        MethodCall request = new MethodCall("doSomething", new Object[] { "42", }, emptyMap);

        Long time = System.currentTimeMillis();
        byte[] checksum = new byte[0];

        SecureRequest secureRequest = new SecureRequest();
        secureRequest.setMessage(request);
        secureRequest.setTimestamp(time);
        secureRequest.setAuthentiation(token);
        secureRequest.setVerification(checksum);

        PublicKey publicKey = new PublicKeyUtil().deserializePublicKey(PUBLIC_KEY_64);

        SecretKeyUtil secretKeyUtil = new SecretKeyUtil("AES");
        SecretKey encryptionKey = secretKeyUtil.generateKey(128);
        BinaryMessageCipher encrypter = new BinaryMessageCipher(new SecretKeyCipherUtil(), encryptionKey);

        byte[] serializedRequest = SerializationUtils.serialize(secureRequest);
        byte[] encryptedRequest = encrypter.encrypt(serializedRequest);

        byte[] encryptedKey = new PublicKeyCipherUtil().encrypt(encryptionKey.getEncoded(), publicKey);

        req.handleRequest(encryptedRequest);
    }
}
