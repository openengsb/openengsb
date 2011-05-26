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

public class JsonSecureRequestMarshallerFilter extends AbstractFilterChainElement<byte[], byte[]> {

    private FilterAction next;

    private ObjectMapper mapper = new ObjectMapper();

    public JsonSecureRequestMarshallerFilter() {
        super(byte[].class, byte[].class);
    }

    @Override
    protected byte[] doFilter(byte[] input, Map<String, Object> metaData) {
        try {
            SecureRequest request = mapper.readValue(input, SecureRequest.class);
            String callId = request.getMessage().getCallId();
            metaData.put("callId", callId);
            resetArgs(request.getMessage());
            SecureResponse response = (SecureResponse) next.filter(request, metaData);
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
    private static void resetArgs(MethodCallRequest request) {
        MethodCall call = request.getMethodCall();
        if (call.getClasses().size() != call.getArgs().length) {
            throw new IllegalArgumentException("Classes and Args have to be the same");
        }
        ObjectMapper mapper = new ObjectMapper();
        Iterator<String> iterator = call.getClasses().iterator();

        List<Object> values = new ArrayList<Object>();

        for (Object arg : call.getArgs()) {
            Class<?> class1;
            try {
                class1 = Class.forName(iterator.next());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            values.add(mapper.convertValue(arg, class1));
        }
        call.setArgs(values.toArray());
    }
}
