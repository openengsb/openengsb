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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.security.filter.MessageCryptoFilterFactory;

public class SecureJavaSerializePortTest extends GenericSecurePortTest<byte[]> {

    MessageCryptoUtil<byte[]> cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());

    @Override
    protected byte[] encodeAndEncrypt(SecureRequest secureRequest, SecretKey sessionKey) throws Exception {
        byte[] serialized = SerializationUtils.serialize(secureRequest);
        byte[] content = cryptoUtil.encrypt(serialized, sessionKey);
        EncryptedMessage message = new EncryptedMessage();
        message.setEncryptedContent(content);
        message.setEncryptedKey(cryptoUtil.encryptKey(sessionKey, serverPublicKey));
        return SerializationUtils.serialize(message);
    }

    @Override
    protected SecureResponse decryptAndDecode(byte[] message, SecretKey sessionKey) throws Exception {
        byte[] content = cryptoUtil.decrypt(message, sessionKey);
        return (SecureResponse) SerializationUtils.deserialize(content);
    }

    @Override
    protected byte[] manipulateMessage(byte[] encryptedRequest) {
        int pos = 187;
        encryptedRequest[pos]++;
        return encryptedRequest;
    }

    @Override
    protected FilterAction getSecureRequestHandlerFilterChain() throws Exception {
        FilterChainElementFactory unpackerFactory = new FilterChainElementFactory() {
            @Override
            public FilterChainElement newInstance() throws FilterConfigurationException {
                return new AbstractFilterChainElement<byte[], byte[]>(byte[].class, byte[].class) {
                    private FilterAction next;

                    @Override
                    protected byte[] doFilter(byte[] input, Map<String, Object> metaData) {
                        EncryptedMessage deserialize = (EncryptedMessage) SerializationUtils.deserialize(input);
                        byte[] result = (byte[]) next.filter(deserialize, metaData);
                        return result;
                    }

                    @Override
                    public void setNext(FilterAction next) throws FilterConfigurationException {
                        this.next = next;
                    }
                };
            }
        };
        FilterChainElementFactory decrypterFactory =
            new MessageCryptoFilterFactory(new SingletonPrivateKeySource(serverPrivateKey));
        FilterChainElementFactory parserFactory = new FilterChainElementFactory() {
            @Override
            public FilterChainElement newInstance() throws FilterConfigurationException {
                return new AbstractFilterChainElement<byte[], byte[]>(byte[].class, byte[].class) {
                    private FilterAction next;

                    @Override
                    protected byte[] doFilter(byte[] input, Map<String, Object> metaData) {
                        SecureRequest deserialize;
                        try {
                            deserialize = (SecureRequest) SerializationUtils.deserialize(input);
                        } catch (SerializationException e) {
                            throw new FilterException(e);
                        }
                        SecureResponse result = (SecureResponse) next.filter(deserialize, metaData);
                        return SerializationUtils.serialize(result);
                    }

                    @Override
                    public void setNext(FilterAction next) throws FilterConfigurationException {
                        this.next = next;
                    }
                };
            }
        };

        FilterChainFactory<byte[], byte[]> factory = new FilterChainFactory<byte[], byte[]>(byte[].class, byte[].class);
        List<Object> asList =
            Arrays.asList(unpackerFactory, decrypterFactory, parserFactory,
                defaultSecureMethodCallFilterFactory.create());
        factory.setFilters(asList);
        return factory.create();
    }
}
