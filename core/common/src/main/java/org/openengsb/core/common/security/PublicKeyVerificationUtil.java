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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class PublicKeyVerificationUtil implements VerificationUtil {

    private static final String DEFAULT_SIGN = "SHA1withRSA";

    private Signature signature;

    public PublicKeyVerificationUtil() {
        init(DEFAULT_SIGN);
    }

    public PublicKeyVerificationUtil(String algorithm) {
        init(algorithm);
    }

    protected void init(String signAlgorithm) {
        try {
            signature = Signature.getInstance(signAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public byte[] sign(byte[] text, Key key) throws SecurityException {
        try {
            return doSign(text, (PrivateKey) key);
        } catch (SignatureException e) {
            throw new SecurityException(e);
        }

    }

    @Override
    public boolean verify(byte[] text, byte[] signature, Key key) {
        try {
            return doVerify(text, signature, (PublicKey) key);
        } catch (SignatureException e) {
            throw new SecurityException("verify failed", e);
        }
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

    public void setSignAlgorithm(String signAlgorithm) {
        try {
            signature = Signature.getInstance(signAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
