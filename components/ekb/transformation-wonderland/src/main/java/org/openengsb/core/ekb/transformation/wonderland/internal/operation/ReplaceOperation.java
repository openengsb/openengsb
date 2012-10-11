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
import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The replace operation is a string operation. It can be used to replace a certain part of the string in the source
 * field with another string.
 */
public class ReplaceOperation extends AbstractStandardTransformationOperation {
    private String oldStringParam = TransformationConstants.REPLACE_OLD_PARAM;
    private String newStringParam = TransformationConstants.REPLACE_NEW_PARAM;
    
    public ReplaceOperation(String operationName) {
        super(operationName, ReplaceOperation.class);
    }

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(getOperationName()).append(" operation is a string operation. It can be used");
        builder.append(" to replace a certain part of the string in the source field with another string.");
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
        checkInputSize(input);
        String oldString = getParameterOrException(parameters, oldStringParam);
        String newString = getParameterOrException(parameters, newStringParam);
        return StringUtils.replace(input.get(0).toString(), oldString, newString);
    }
}
