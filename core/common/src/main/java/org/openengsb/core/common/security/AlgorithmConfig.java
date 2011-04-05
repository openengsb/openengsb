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

public class AlgorithmConfig {

    private String publicKeyAlgorithm;
    private int publicKeySize;
    private String secretKeyAlgorithm;
    private int secretKeySize;

    public AlgorithmConfig() {
    }

    public AlgorithmConfig(String publicKeyAlgorithm, int publicKeySize, String secretKeyAlgorithm, int secretKeySize) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
        this.publicKeySize = publicKeySize;
        this.secretKeyAlgorithm = secretKeyAlgorithm;
        this.secretKeySize = secretKeySize;
    }

    public static AlgorithmConfig getDefault() {
        return new AlgorithmConfig("RSA", 2048, "AES", 128);
    }

    public String getPublicKeyAlgorithm() {
        return this.publicKeyAlgorithm;
    }

    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }

    public int getPublicKeySize() {
        return this.publicKeySize;
    }

    public void setPublicKeySize(int publicKeySize) {
        this.publicKeySize = publicKeySize;
    }

    public String getSecretKeyAlgorithm() {
        return this.secretKeyAlgorithm;
    }

    public void setSecretKeyAlgorithm(String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    public int getSecretKeySize() {
        return this.secretKeySize;
    }

    public void setSecretKeySize(int secretKeySize) {
        this.secretKeySize = secretKeySize;
    }

}
