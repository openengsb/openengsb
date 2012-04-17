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
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.openengsb.core.api.remote.MethodResultMessage;

/**
 * This filter takes a {@link MethodCallRequest} and serializes it to JSON. The String s then passed on to the next
 * filter. The returned JSON-String representing a {@link MethodResultMessage} is then deserialized and returned.
 * 
 * <code>
 * <pre>
 *      [MethodCallRequest]   > Filter > [MethodCallRequest as JSON-string]     > ...
 *                                                                                 |
 *                                                                                 v
 *      [MethodResultMessage] < Filter < [MethodResultMessage as JSON-string]   < ...
 * </pre>
 * </code>
 */
public class JsonOutgoingMethodCallMarshalFilter extends
        AbstractFilterChainElement<MethodCallRequest, MethodResultMessage> {

    private FilterAction next;
    private GenericObjectSerializer objectSerializer;

    public JsonOutgoingMethodCallMarshalFilter(GenericObjectSerializer objectSerializer) {
        super(MethodCallRequest.class, MethodResultMessage.class);
        this.objectSerializer = objectSerializer;
    }

    @Override
    public MethodResultMessage doFilter(MethodCallRequest input, Map<String, Object> metadata) throws FilterException {
        MethodResultMessage resultMessage;
        try {
            String jsonString = objectSerializer.serializeToString(input);
            String resultString = (String) next.filter(jsonString, metadata);
            if (resultString == null) {
                return null;
            }
            resultMessage = objectSerializer.parse(resultString, MethodResultMessage.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        MethodResult result = resultMessage.getResult();
        if (result.getType().equals(ReturnType.Void)) {
            result.setArg(null);
        }
        return resultMessage;
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, String.class, String.class);
        this.next = next;
    }

    public void setObjectSerializer(GenericObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

}
