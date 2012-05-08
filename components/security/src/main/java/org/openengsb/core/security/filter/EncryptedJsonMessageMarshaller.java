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

package org.openengsb.core.security.filter;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter takes a {@link String} representing a JSON-encoded {@link EncryptedMessage} and parses it. The actual
 * decryption is left to the next filter. The next filter returns a byte[] with the result that has been encrypted with
 * the session-key. It is just Base64-encoded for transport.
 *
 * This filter is intended for incoming ports.
 *
 * <code>
 * <pre>
 *      [EncryptedMessage(content+encryptedKey) as String] > Filter > [EncryptedMessage-object]    > ...
 *                                                                                                   |
 *                                                                                                   v
 *      [Base64-encoded String of encrypted result]        < Filter < [encrypted result as byte[]] < ...
 * </pre>
 * </code>
 */
public class EncryptedJsonMessageMarshaller extends AbstractFilterChainElement<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedJsonMessageMarshaller.class);

    private FilterAction next;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String doFilter(String input, Map<String, Object> metaData) {
        EncryptedMessage message;
        try {
            LOGGER.debug("attempting to parse encrypted json message");
            message = objectMapper.readValue(input, EncryptedMessage.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        LOGGER.info("Encrypted message parsed sucessfully, passing to next filter {}", next.getClass().getName());
        byte[] result = (byte[]) next.filter(message, metaData);
        return Base64.encodeBase64String(result);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, EncryptedMessage.class, byte[].class);
        this.next = next;
    }

}
