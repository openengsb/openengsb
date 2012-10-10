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

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The replace operation is a string operation. It can be used to replace a certain part of the string in the source
 * field with another string.
 */
public class ReplaceOperation implements TransformationOperation {
    private String operationName = "replace";
    private String oldStringParam = "oldString";
    private String newStringParam = "newString";

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(operationName).append(" operation is a string operation. It can be used to ");
        builder.append("replace a certain part of the string in the source field with another string.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(oldStringParam, "The string that should be replaced.");
        parameters.put(newStringParam, "The string which should replace the old one.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }
        String oldString = parameters.get(oldStringParam);
        String newString = parameters.get(newStringParam);
        if (oldString == null || newString == null) {
            throw new TransformationOperationException(
                "The old string and/or the new string parameter is not defined.");
        }
        return StringUtils.replace(input.get(0).toString(), oldString, newString);
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

}
