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
import org.openengsb.core.api.security.DecryptionException;
import org.openengsb.core.api.security.EncryptionException;
import org.openengsb.core.api.security.MarshalException;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

public abstract class SecureRequestHandler<EncodingType> {

    private RequestHandler realHandler;
    private MessageCryptoUtil<EncodingType> messageCryptoUtil;
    private PrivateKey privateKey;
    private AuthenticationManager authManager;
    private MessageVerifier messageVerifier;

    public abstract SecureRequest unmarshalRequest(EncodingType decrypt) throws MarshalException;

    public abstract EncryptedMessage<EncodingType> unmarshalContainer(EncodingType container) throws MarshalException;

    public abstract EncodingType marshalResponse(SecureResponse response) throws MarshalException;

    public EncodingType handleRequest(EncodingType containerMessage) throws DecryptionException, EncryptionException {
        EncryptedMessage<EncodingType> container = unmarshalContainer(containerMessage);

        EncodingType encryptedKey = container.getEncryptedKey();
        SecretKey sessionKey;
        sessionKey = messageCryptoUtil.decryptKey(encryptedKey, privateKey);

        EncodingType encryptedContent = container.getEncryptedContent();
        EncodingType decrypt;
        decrypt = messageCryptoUtil.decrypt(encryptedContent, sessionKey);

        SecureRequest secureRequest = unmarshalRequest(decrypt);

        messageVerifier.verify(secureRequest);

        Authentication springAuthentication =
            secureRequest.retrieveAuthenticationInfo().toSpringSecurityAuthentication();
        authManager.authenticate(springAuthentication);

        authManager.authenticate(secureRequest.retrieveAuthenticationInfo().toSpringSecurityAuthentication());
        MethodResult methodReturn = realHandler.handleCall(secureRequest.getMessage());

        SecureResponse secureResponse = SecureResponse.create(methodReturn);
        EncodingType response = marshalResponse(secureResponse);

        return messageCryptoUtil.encrypt(response, sessionKey);
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

    public void setMessageVerifier(MessageVerifier messageVerifier) {
        this.messageVerifier = messageVerifier;
    }

}
