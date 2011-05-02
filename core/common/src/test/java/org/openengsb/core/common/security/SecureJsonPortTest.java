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

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.security.MarshalException;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;

public class SecureJsonPortTest extends GenericSecurePortTest<String> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected MessageCryptoUtil<String> getMessageCryptoUtil() {
        return new StringMessageCryptoUtil(AlgorithmConfig.getDefault());
    }

    @Override
    protected SecureRequestHandler<String> getSecureRequestHandler() {
        return new SecureRequestHandler<String>() {
            @Override
            public SecureRequest unmarshalRequest(String decrypt) {
                try {
                    return mapper.readValue(decrypt, SecureRequest.class);
                } catch (IOException e) {
                    throw new MarshalException(e);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public EncryptedMessage<String> unmarshalContainer(String container) {
                try {
                    return mapper.readValue(container, EncryptedMessage.class);
                } catch (IOException e) {
                    throw new MarshalException(e);
                }
            }

            @Override
            public String marshalResponse(SecureResponse response) {
                try {
                    return mapper.writeValueAsString(response);
                } catch (IOException e) {
                    throw new MarshalException(e);
                }
            }
        };
    }

    @Override
    protected String encode(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    @Override
    protected <T> T decode(String encoded, Class<T> objectClass) throws Exception {
        return mapper.readValue(encoded, objectClass);
    }

}
