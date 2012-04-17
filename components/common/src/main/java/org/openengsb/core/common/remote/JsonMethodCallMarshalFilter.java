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

package org.openengsb.core.common.remote;

import java.io.IOException;
import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.GenericObjectSerializer;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;

/**
 * This filter takes a JSON-serialized {@link MethodCallRequest} and deserializes it. The {@link MethodCallRequest}
 * object is then passed on to the next filter. The returned {@link MethodResultMessage} is than seralized to JSON
 * again.
 * 
 * <code>
 * <pre>
 *      [MethodCallRequest as JSON-string]   > Filter > [MethodCallRequest]     > ...
 *                                                                                 |
 *                                                                                 v
 *      [MethodResultMessage as JSON-string] < Filter < [MethodResultMessage]   < ...
 * </pre>
 * </code>
 */
public class JsonMethodCallMarshalFilter extends AbstractFilterChainElement<String, String> {

    private FilterAction next;

    private GenericObjectSerializer objectSerializer;

    public JsonMethodCallMarshalFilter(GenericObjectSerializer objectSerializer) {
        super(String.class, String.class);
        this.objectSerializer = objectSerializer;
    }

    @Override
    public String doFilter(String input, Map<String, Object> metadata) throws FilterException {
        MethodCallRequest call;
        try {
            call = objectSerializer.parse(input, MethodCallRequest.class);
            MethodResultMessage returnValue = (MethodResultMessage) next.filter(call, metadata);
            return objectSerializer.serializeToString(returnValue);
        } catch (IOException e) {
            throw new FilterException(e);
        }
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
        this.next = next;
    }

    public void setObjectSerializer(GenericObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

}
