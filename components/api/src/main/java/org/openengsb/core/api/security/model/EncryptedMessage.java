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

package org.openengsb.core.api.security.model;

import java.io.Serializable;

/**
 * Representation of an encrypted message that is ready to be serialized and transported. When transporting an instance
 * of this class, the content is supposed to be encrypted.
 *
 * This should only be used for incoming messages as the session-key is encrypted using asymmetric cryptography.
 *
 * This is the way a client is supposed to create a request:
 * <ul>
 * <li>marshal the message to a byte[]</li>
 * <li>generate a session-key with using the correct algorithm (default is AES-128)</li>
 * <li>encrypt the message with the session-key</li>
 * <li>encrypt the session-key with the server's public key</li>
 * </ul>
 *
 * The response to the client is then encrypted with the same session-key
 */
@SuppressWarnings("serial")
public class EncryptedMessage implements Serializable {

    /**
     * Contains the content of the message to transport, encrypted with the sessionKey.
     */
    private byte[] encryptedContent;
    /**
     * Contains the encrypted Session key (that has been encrypted using the servers public key)
     */
    private byte[] encryptedKey;

    public EncryptedMessage() {
    }

    public EncryptedMessage(byte[] encryptedContent, byte[] encryptedKey) {
        this.encryptedContent = encryptedContent;
        this.encryptedKey = encryptedKey;
    }

    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(byte[] encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

}
