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

import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;

public class SecureBinaryPortTest extends GenericSecurePortTest<byte[]> {

    @Override
    protected MessageCryptoUtil<byte[]> getMessageCryptoUtil() {
        return new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());
    }

    @Override
    protected SecureRequestHandler<byte[]> getSecureRequestHandler() {
        return new SecureRequestHandler<byte[]>() {

            @Override
            public SecureRequest unmarshalRequest(byte[] decrypt) {
                return (SecureRequest) SerializationUtils.deserialize(decrypt);
            }

            @SuppressWarnings("unchecked")
            @Override
            public EncryptedMessage<byte[]> unmarshalContainer(byte[] container) {
                return (EncryptedMessage<byte[]>) SerializationUtils.deserialize(container);
            }

            @Override
            public byte[] marshalResponse(SecureResponse response) {
                return SerializationUtils.serialize(response);
            }
        };
    }

    @Override
    protected byte[] encode(Object o) {
        return SerializationUtils.serialize((Serializable) o);
    }

    @Override
    protected Object decode(byte[] encoded) {
        return SerializationUtils.deserialize(encoded);
    }

}
