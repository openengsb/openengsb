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

import javax.crypto.SecretKey;

import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.springframework.security.authentication.AuthenticationManager;

public abstract class SecureRequestHandler {

    private RequestHandler realHandler;
    private CipherUtil cipherUtil = new SecretKeyCipherUtil();
    private KeyDecrypter keyDecrypter;
    private AuthenticationManager authManager;

    public abstract SecureRequest unmarshalRequest(byte[] decrypt);

    public abstract EncryptedMessage unmarshalContainer(byte[] container);

    public void handleRequest(byte[] containerMessage) {
        EncryptedMessage container = unmarshalContainer(containerMessage);
        byte[] encryptedKey = container.getEncryptedKey();
        SecretKey sessionKey = keyDecrypter.decryptKey(encryptedKey);
        byte[] encContent = container.getEncryptedContent();
        byte[] decrypt = cipherUtil.decrypt(encContent, sessionKey);
        SecureRequest secureRequest = unmarshalRequest(decrypt);
        secureRequest.verify();
        authManager.authenticate(secureRequest.getAuthentiation());
        MethodResult methodReturn = realHandler.handleCall(secureRequest.getMessage());
    }

    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void setRealHandler(RequestHandler realHandler) {
        this.realHandler = realHandler;
    }

    public void setKeyDecrypter(KeyDecrypter keyDecrypter) {
        this.keyDecrypter = keyDecrypter;
    }

    public void setCipherUtil(CipherUtil cipherUtil) {
        this.cipherUtil = cipherUtil;
    }
}
