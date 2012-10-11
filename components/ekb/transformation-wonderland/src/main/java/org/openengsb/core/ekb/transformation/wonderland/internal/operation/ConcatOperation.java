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

import com.google.common.base.Joiner;

/**
 * The concat operation concatenates all source field values of the operation and use the concat string parameter as
 * string which is put between the field values.
 */
public class ConcatOperation extends AbstractStandardTransformationOperation {
    private String concatStringParam = TransformationConstants.CONCAT_PARAM;

    public ConcatOperation(String operationName) {
        super(operationName, ConcatOperation.class);
    }

    @Override
    public String getOperationDescription() {
        return theOperation().does("concatenates all source fields with the given concatenation string.")
            .toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return -1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(concatStringParam, "Defines the string which is placed between the concatenated values");
        return params;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        checkInputSize(input);
        String concatString = getParameterOrDefault(parameters, concatStringParam, "");
        return Joiner.on(concatString).join(input);
    }
}
