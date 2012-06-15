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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.common.util.JsonUtils;
import org.openengsb.core.common.util.ModelUtils;

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
public class JsonOutgoingMethodCallMarshalFilter extends
        AbstractFilterChainElement<MethodCallMessage, MethodResultMessage> {

    private FilterAction next;

    @Override
    public MethodResultMessage doFilter(MethodCallMessage input, Map<String, Object> metadata) throws FilterException {
        ObjectMapper objectMapper = JsonUtils.createObjectMapperWithIntroSpectors();
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
            if (resultType.isInterface() || Modifier.isAbstract(resultType.getModifiers())) {
                result.setArg(convertToOpenEngSBModel(result.getArg(), resultType));
            } else {
                Object convertedValue = objectMapper.convertValue(result.getArg(), resultType);
                result.setArg(convertedValue);
            }
        }
        return resultMessage;
    }

    private static OpenEngSBModel convertToOpenEngSBModel(Object arg, Class<?> resultType) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) arg;
        for (Map.Entry<String, Object> dataEntry : data.entrySet()) {
            try {
                Class<?> attributeType = getAttributeType(resultType, dataEntry.getKey());
                Object value = dataEntry.getValue();
                if (Number.class.isAssignableFrom(attributeType)) {
                    value = NumberUtils.createNumber((String) value);
                }
                entries.add(new OpenEngSBModelEntry(dataEntry.getKey(), value, attributeType));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return (OpenEngSBModel) ModelUtils.createModelObject(resultType, entries.toArray(new OpenEngSBModelEntry[0]));
    }

    private static Class<?> getAttributeType(Class<?> clazz, String attributeName) throws NoSuchMethodException {
        return clazz.getMethod("get" + StringUtils.capitalize(attributeName)).getReturnType();
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, String.class, String.class);
        this.next = next;
    }

}
