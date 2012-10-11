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

import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The split operation operation maps keys to values. Every value to be mapped is a key in the parameter map and the
 * value to this key defines the mapped value. If there is no mapping defined for the given value, a
 * TransformationOperationException is thrown.
 */
public class MapOperation extends AbstractStandardTransformationOperation {

    public MapOperation(String operationName) {
        super(operationName, MapOperation.class);
    }

    @Override
    public String getOperationDescription() {
        return getOperationDescriptor().does("maps keys to values. Every key is a key in the parameter map and ")
            .cnt("the value to this key defines the mapped value.").toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("any parameter", "Put a key value pair for every mapping you want in the parameters.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        checkInputSize(input);
        Object value = parameters.get(input.get(0));
        if (value == null) {
            throw new TransformationOperationException("For the given key is no mapping defined");
        }
        return value;
    }
}
