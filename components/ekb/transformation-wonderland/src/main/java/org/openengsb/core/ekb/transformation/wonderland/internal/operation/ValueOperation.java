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

package org.openengsb.core.ekb.transformation.wonderland.internal.operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The value operation returns a constant value.
 */
public class ValueOperation extends AbstractStandardTransformationOperation {
    private String valueParam = TransformationConstants.VALUE_PARAM;
    
    public ValueOperation(String operationName) {
        super(operationName);
    }    

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(getOperationName()).append(" operation returns a constant value.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 0;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(valueParam, "Defines the string which should be returned by the value operation."
                + " If this parameter is not defined, the empty string will be used.");
        return params;
    }

    @Override
    public Object performOperation(List<Object> inputs, Map<String, String> parameters)
        throws TransformationOperationException {
        String value = parameters.containsKey(valueParam) ? parameters.get(valueParam) : "";
        return value;
    }
}
