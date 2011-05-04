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

import org.apache.commons.lang.SerializationUtils;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;

public class SecureJavaSerializePortTest extends GenericSecurePortTest<byte[]> {

    MessageCryptoUtil<byte[]> cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());

    @Override
    protected byte[] encodeAndEncrypt(SecureRequest secureRequest, SecretKey sessionKey) throws Exception {
        byte[] serialized = SerializationUtils.serialize(secureRequest);
        return cryptoUtil.encrypt(serialized, sessionKey);
    }

    @Override
    protected SecureResponse decryptAndDecode(byte[] message, SecretKey sessionKey) throws Exception {
        byte[] content = cryptoUtil.decrypt(message, sessionKey);
        return (SecureResponse) SerializationUtils.deserialize(content);
    }

    @Override
    protected FilterAction getSecureRequestHandlerFilterChain() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
