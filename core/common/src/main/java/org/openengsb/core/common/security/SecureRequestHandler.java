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

import javax.crypto.SecretKey;

import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.DecryptionException;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.EncryptionException;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.springframework.security.authentication.AuthenticationManager;

public abstract class SecureRequestHandler<EncodingType> {

    private RequestHandler realHandler;
    private MessageCryptoUtil<EncodingType> messageCryptoUtil;
    private PrivateKey privateKey;
    private AuthenticationManager authManager;

    public abstract SecureRequest unmarshalRequest(EncodingType decrypt);

    public abstract EncryptedMessage<EncodingType> unmarshalContainer(EncodingType container);

    public abstract EncodingType marshalResponse(SecureResponse response);

    public EncodingType handleRequest(EncodingType containerMessage) {
        EncryptedMessage<EncodingType> container = unmarshalContainer(containerMessage);

        EncodingType encryptedKey = container.getEncryptedKey();
        SecretKey sessionKey;
        try {
            sessionKey = messageCryptoUtil.decryptKey(encryptedKey, privateKey);
        } catch (DecryptionException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

        EncodingType encryptedContent = container.getEncryptedContent();
        EncodingType decrypt;
        try {
            decrypt = messageCryptoUtil.decrypt(encryptedContent, sessionKey);
        } catch (DecryptionException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

        SecureRequest secureRequest = unmarshalRequest(decrypt);
        secureRequest.verify();
        authManager.authenticate(secureRequest.getAuthentiation());
        MethodResult methodReturn = realHandler.handleCall(secureRequest.getMessage());
        SecureResponse secureResponse = SecureResponse.create(methodReturn);
        EncodingType response = marshalResponse(secureResponse);

        try {
            return messageCryptoUtil.encrypt(response, sessionKey);
        } catch (EncryptionException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void setRealHandler(RequestHandler realHandler) {
        this.realHandler = realHandler;
    }

    public void setCryptUtil(MessageCryptoUtil<EncodingType> cryptUtil) {
        this.messageCryptoUtil = cryptUtil;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

}
