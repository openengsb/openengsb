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

import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The forward operation just copies the given input object and returns it.
 */
public class ForwardOperation implements TransformationOperation {
    private String operationName = "forward";

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(operationName).append(" operation just returns the given input value.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        return new HashMap<String, String>();
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters) throws TransformationOperationException {
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException("The input values are not matching with the operation input count.");
        }
        return input.get(0);
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
