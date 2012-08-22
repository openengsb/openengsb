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

package org.openengsb.core.ekb.api.transformation;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes a transforming step in the transformation progress. Contains all informations to perform a transformation
 * operation on source fields and target fields.
 */
public class TransformationStep {
    private String targetField;
    private TransformationOperation operation;
    private Map<String, String> operationParams;
    private String[] sourceFields;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public TransformationOperation getOperation() {
        return operation;
    }

    public void setOperation(TransformationOperation operation) {
        this.operation = operation;
    }

    public Map<String, String> getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(Map<String, String> operationParams) {
        this.operationParams = operationParams;
    }

    public String getOperationParamater(String key) {
        if (operationParams == null) {
            return null;
        }
        return operationParams.get(key);
    }

    public void setOperationParameter(String key, String value) {
        if (operationParams == null) {
            operationParams = new HashMap<String, String>();
        }
        operationParams.put(key, value);
    }

    public String[] getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(String... sourceFields) {
        this.sourceFields = sourceFields;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ").append(operation);
        builder.append(" sources: {");
        boolean firstSource = true;
        for (String source : sourceFields) {
            if (!firstSource) {
                builder.append(", ");
            }
            builder.append(source);
            firstSource = false;
        }
        builder.append("} ");
        builder.append(" target: {");
        builder.append(targetField).append("}}");
        return builder.toString();
    }
}
