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

package org.openengsb.core.services.filter;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

/**
 * This filter takes a {@link MethodCallMessage} and serializes it to JSON. The String s then passed on to the next
 * filter. The returned JSON-String representing a {@link MethodResultMessage} is then deserialized and returned.
 * 
 * <code>
 * <pre>
 *      [MethodCallMessage]   > Filter > [MethodCallMessage as JSON-string]     > ...
 *                                                                                 |
 *                                                                                 v
 *      [MethodResultMessage] < Filter < [MethodResultMessage as JSON-string]   < ...
 * </pre>
 * </code>
 */
public class OutgoingJsonSecureMethodCallMarshalFilter extends
        AbstractFilterChainElement<MethodCallMessage, MethodResultMessage> {

    private FilterAction next;

    @Override
    public MethodResultMessage doFilter(MethodCallMessage input, Map<String, Object> metadata) throws FilterException {
        ObjectMapper objectMapper = createObjectMapper();
        MethodResultMessage resultMessage;
        try {
            String jsonString = objectMapper.writeValueAsString(input);
            String resultString = (String) next.filter(jsonString, metadata);
            if (resultString == null) {
                return null;
            }
            resultMessage = objectMapper.readValue(resultString, MethodResultMessage.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        MethodResult result = resultMessage.getResult();

        if (result.getType().equals(ReturnType.Void)) {
            result.setArg(null);
        } else {
            Class<?> resultType;
            try {
                resultType = Class.forName(result.getClassName());
            } catch (ClassNotFoundException e) {
                throw new FilterException(e);
            }
            Object convertedValue = objectMapper.convertValue(result.getArg(), resultType);
            result.setArg(convertedValue);
        }
        return resultMessage;
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, String.class, String.class);
        this.next = next;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector primaryIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondaryIntrospector = new JaxbAnnotationIntrospector();
        AnnotationIntrospector introspector =
            new AnnotationIntrospector.Pair(primaryIntrospector, secondaryIntrospector);
        mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        return mapper;
    }

}
