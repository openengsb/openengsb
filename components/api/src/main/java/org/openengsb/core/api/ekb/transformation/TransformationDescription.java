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

package org.openengsb.core.api.ekb.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes all steps to transform an object of the source class into an object of the target class.
 */
public class TransformationDescription {
    private Class<?> source;
    private Class<?> target;
    private Map<String, TransformationStep> steps;

    public TransformationDescription(Class<?> source, Class<?> target) {
        this.source = source;
        this.target = target;
        steps = new HashMap<String, TransformationStep>();
    }

    public Class<?> getSource() {
        return source;
    }

    public Class<?> getTarget() {
        return target;
    }

    /**
     * Adds a forward transformation step to the transformation description. The value of the source field is copied to
     * the target field unchanged. Both fields need to have the same object type.
     */
    public void forwardField(String sourceField, String targetField) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperation(TransformationOperation.FORWARD);
        steps.put(targetField, step);
    }

    /**
     * Adds a concat transformation step to the transformation description. The values of the source fields are
     * concatenated to the target field with the concat string between the source fields values. All fields need to be
     * of the type String.
     */
    public void concatField(String targetField, String concatString, String... sourceFields) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceFields);
        step.setOperationParam(concatString);
        step.setOperation(TransformationOperation.CONCAT);
        steps.put(targetField, step);
    }

    /**
     * Adds a split transformation step to the transformation description. The value of the source field is split based
     * on the split string into parts. This parts are then set at the target fields. if the result of the split
     * operation has too few / too many results for the target fields, a warning is printed. All fields need to be of
     * the type String.
     */
    public void splitField(String sourceField, String splitString, String... targetFields) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(sourceField);
        step.setSourceFields(targetFields);
        step.setOperationParam(splitString);
        step.setOperation(TransformationOperation.SPLIT);
        StringBuilder key = new StringBuilder();
        for (String target : targetFields) {
            if (key.length() != 0) {
                key.append("; ");
            }
            key.append(target);
        }
        steps.put(key.toString(), step);
    }

    public List<TransformationStep> getTransformingSteps() {
        return new ArrayList<TransformationStep>(steps.values());
    }
}
