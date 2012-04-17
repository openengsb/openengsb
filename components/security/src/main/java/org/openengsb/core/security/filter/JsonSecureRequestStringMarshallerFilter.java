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

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.GenericObjectSerializer;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter takes a {@link String} representing a JSON-encoded {@link SecureRequest} and parses it. The next filter
 * returns a SecureResponse which is marshaled to JSON again.
 * 
 * This filter is intended for incoming ports.
 * 
 * <code>
 * <pre>
 *      [JSON-String]         > Filter > [SecureRequest]    > ...
 *                                                             |
 *                                                             v
 *      [JSON-String]         < Filter < [SecureResponse]     < ...
 * </pre>
 * </code>
 */
public class JsonSecureRequestStringMarshallerFilter extends AbstractFilterChainElement<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSecureRequestStringMarshallerFilter.class);

    private FilterAction next;

    private GenericObjectSerializer objectSerializer;

    public JsonSecureRequestStringMarshallerFilter(GenericObjectSerializer objectSerializer) {
        super(String.class, String.class);
        this.objectSerializer = objectSerializer;
    }

    @Override
    protected String doFilter(String input, Map<String, Object> metaData) {
        SecureRequest request;
        try {
            LOGGER.trace("attempt to read SecureRequest from inputData");
            request = objectSerializer.parse(input, SecureRequest.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        String callId = request.getMessage().getCallId();
        LOGGER.info("extracted callId \"{}\" from message", callId);
        metaData.put("callId", callId);
        LOGGER.debug("invoking next filter: {}", next.getClass().getName());
        SecureResponse response = (SecureResponse) next.filter(request, metaData);
        LOGGER.debug("response received for callId {}: {}. serializing to json", callId, response);
        try {
            return objectSerializer.serializeToString(response);
        } catch (IOException e) {
            throw new FilterException(e);
        }

    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }
}
