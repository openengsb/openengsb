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

package org.openengsb.core.common.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

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
public class JsonSecureRequestMarshallerFilter extends AbstractFilterChainElement<byte[], byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSecureRequestMarshallerFilter.class);

    private FilterAction next;

    private ObjectMapper mapper = new ObjectMapper();

    public JsonSecureRequestMarshallerFilter() {
        super(byte[].class, byte[].class);
    }

    @Override
    protected byte[] doFilter(byte[] input, Map<String, Object> metaData) {
        SecureRequest request;
        try {
            LOGGER.trace("attempt to read SecureRequest from inputData");
            request = mapper.readValue(input, SecureRequest.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        String callId = request.getMessage().getCallId();
        LOGGER.info("extracted callId \"{}\" from message", callId);
        metaData.put("callId", callId);
        LOGGER.debug("converting arguments of inputmessage");
        resetArgs(request.getMessage());
        LOGGER.debug("invoking next filter: {}", next.getClass().getName());
        SecureResponse response = (SecureResponse) next.filter(request, metaData);
        LOGGER.debug("response received for callId {}: {}. serializing to json", callId, response);
        try {
            return mapper.writeValueAsBytes(response);
        } catch (IOException e) {
            throw new FilterException(e);
        }

    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }

    /**
     * Converts the Args read by Jackson into the correct classes that have to be used for calling the method.
     */
    private void resetArgs(MethodCallRequest request) {
        MethodCall call = request.getMethodCall();
        Preconditions.checkArgument(call.getClasses().size() == call.getArgs().length,
            "length of args and their types does not match");
        List<Class<?>> classList;
        try {
            LOGGER.debug("loading classes referenced in arguments");
            classList = convertClassesToClassNames(call.getClasses());
        } catch (ClassNotFoundException e) {
            throw new FilterException(e);
        }
        Iterator<Class<?>> iterator = classList.iterator();
        List<Object> values = new ArrayList<Object>();
        for (Object arg : call.getArgs()) {
            Class<?> class1 = iterator.next();
            LOGGER.debug("converting argument to type {} ({})", class1.getName(), arg);
            values.add(mapper.convertValue(arg, class1));
        }
        call.setArgs(values.toArray());
    }

    private List<Class<?>> convertClassesToClassNames(List<String> classes) throws ClassNotFoundException {
        List<Class<?>> result = new ArrayList<Class<?>>(classes.size());
        for (String className : classes) {
            LOGGER.debug("try to load class {} to unmarshal argument", className);
            result.add(Class.forName(className));
            LOGGER.debug("{} loaded successfully", className);
        }
        return result;
    }
}
