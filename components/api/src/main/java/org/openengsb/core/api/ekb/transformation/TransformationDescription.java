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
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.ekb.TransformationConstants;

/**
 * Describes all steps to transform an object of the source class into an object of the target class.
 */
public class TransformationDescription {
    private Class<?> source;
    private Class<?> target;
    private List<TransformationStep> steps;

    public TransformationDescription(Class<?> source, Class<?> target) {
        this.source = source;
        this.target = target;
        steps = new ArrayList<TransformationStep>();
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
        steps.add(step);
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
        step.setOperationParameter(TransformationConstants.concatParam, concatString);
        step.setOperation(TransformationOperation.CONCAT);
        steps.add(step);
    }

    /**
     * Adds a split transformation step to the transformation description. The value of the source field is split based
     * on the split string into parts. Based on the given index, the result will be set to the target field. The index
     * needs to be an integer value. All fields need to be of the type String.
     */
    public void splitField(String sourceField, String targetField, String splitString, String index) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperationParameter(TransformationConstants.splitParam, splitString);
        step.setOperationParameter(TransformationConstants.index, index);
        step.setOperation(TransformationOperation.SPLIT);
        steps.add(step);
    }

    /**
     * Adds a map transformation step to the transformation description. The value of the source field is mapped based
     * on the mapping to another value which is forwarded to the target field. The values in the models need to be
     * working with the values of the mapping (e.g. assigning 'hello' to Integer field will not work).
     */
    public void mapField(String sourceField, String targetField, Map<String, String> mapping) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperationParams(mapping);
        step.setOperation(TransformationOperation.MAP);
        steps.add(step);
    }

    /**
     * Adds a substring step to the transformation description. The value of the source field is taken and it is tried
     * to build the substring from the given index from to the given index to. From and to must be Integers written as
     * String, the value of the source field must also be a String.
     */
    public void substringField(String sourceField, String targetField, String from, String to) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperationParameter(TransformationConstants.substringFrom, from);
        step.setOperationParameter(TransformationConstants.substringTo, to);
        step.setOperation(TransformationOperation.SUBSTRING);
        steps.add(step);
    }
    
    /**
     * Adds a value step to the transformation description. The given value is written to the target field.
     */
    public void valueField(String targetField, String value) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.value, value);
        step.setOperation(TransformationOperation.VALUE);
        steps.add(step);
    }

    public List<TransformationStep> getTransformingSteps() {
        return steps;
    }
}
