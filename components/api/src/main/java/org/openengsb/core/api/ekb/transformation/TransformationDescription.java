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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes all steps to transform an object of the source class into an object of the target class.
 */
public class TransformationDescription {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationDescription.class);
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

    public void addStep(TransformationOperation operation, List<String> sourceFields, String targetField,
            Map<String, String> parameters) {
        switch (operation) {
            case FORWARD:
                forwardField(sourceFields.get(0), targetField);
                break;
            case CONCAT:
                String concatString = parameters.get(TransformationConstants.concatParam);
                concatField(targetField, concatString, sourceFields.toArray(new String[0]));
                break;
            case LENGTH:
                String lengthFunction = parameters.get(TransformationConstants.lengthFunction);
                lengthField(sourceFields.get(0), targetField, lengthFunction);
                break;
            case MAP:
                mapField(sourceFields.get(0), targetField, parameters);
                break;
            case SUBSTRING:
                String from = parameters.get(TransformationConstants.substringFrom);
                String to = parameters.get(TransformationConstants.substringTo);
                substringField(sourceFields.get(0), targetField, from, to);
                break;
            case SPLIT:
                String splitString = parameters.get(TransformationConstants.splitParam);
                String index = parameters.get(TransformationConstants.index);
                splitField(sourceFields.get(0), targetField, splitString, index);
                break;
            case SPLITREGEX:
                splitString = parameters.get(TransformationConstants.splitParam);
                index = parameters.get(TransformationConstants.index);
                splitRegexField(sourceFields.get(0), targetField, splitString, index);
                break;
            case TOLOWER:
                toLowerField(sourceFields.get(0), targetField);
                break;
            case TOUPPER:
                toUpperField(sourceFields.get(0), targetField);
                break;
            case TRIM:
                trimField(sourceFields.get(0), targetField);
                break;
            case VALUE:
                String value = parameters.get(TransformationConstants.value);
                valueField(targetField, value);
                break;
            case REPLACE:
                String oldString = parameters.get(TransformationConstants.replaceOld);
                String newString = parameters.get(TransformationConstants.replaceNew);
                replaceField(sourceFields.get(0), targetField, oldString, newString);
                break;
            case REVERSE:
                reverseField(sourceFields.get(0), targetField);
                break;
            case PAD:
                String length = parameters.get(TransformationConstants.padLength);
                String character = parameters.get(TransformationConstants.padCharacter);
                String direction = parameters.get(TransformationConstants.padDirection);
                padField(sourceFields.get(0), targetField, length, character, direction);
                break;
            case NONE:
            default:
                LOGGER.warn("Not supported operation received: " + operation);
                break;
        }
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
     * Adds a split regex transformation step to the transformation description. The value of the source field is split
     * based on the split string as regular expression into parts. Based on the given index, the result will be set to
     * the target field. The index needs to be an integer value. All fields need to be of the type String.
     */
    public void splitRegexField(String sourceField, String targetField, String splitString, String index) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperationParameter(TransformationConstants.splitParam, splitString);
        step.setOperationParameter(TransformationConstants.index, index);
        step.setOperation(TransformationOperation.SPLITREGEX);
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

    /**
     * Adds a length step to the transformation description. The step takes the object from the source field and
     * calculate the length through the given method function (which needs to be implemented by the object). The result
     * will be written in the target field. If the function is null, then "length" will be taken as standard function.
     */
    public void lengthField(String sourceField, String targetField, String function) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.lengthFunction, function);
        step.setOperation(TransformationOperation.LENGTH);
        steps.add(step);
    }

    /**
     * Adds a trim step to the transformation description. The source field and the target field need to be of String
     * type. Performs standard trim operation on the string of the source field and writes the result to the target
     * field.
     */
    public void trimField(String sourceField, String targetField) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperation(TransformationOperation.TRIM);
        steps.add(step);
    }

    /**
     * Adds a toLower step to the transformation description. The source and the target field need to be of String type.
     * Performs standard toLower operation on the string of the source field and writes the result to the target field.
     */
    public void toLowerField(String sourceField, String targetField) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperation(TransformationOperation.TOLOWER);
        steps.add(step);
    }

    /**
     * Adds a toUpper step to the transformation description. The source and the target field need to be of String type.
     * Performs standard toUpper operation on the string of the source field and writes the result to the target field.
     */
    public void toUpperField(String sourceField, String targetField) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperation(TransformationOperation.TOUPPER);
        steps.add(step);
    }

    /**
     * Adds a replace step to the transformation description. The source and the target field need to be of String type.
     * Performs standard string replacement on the string of the source field and writes the result to the target field.
     */
    public void replaceField(String sourceField, String targetField, String oldString, String newString) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.replaceOld, oldString);
        step.setOperationParameter(TransformationConstants.replaceNew, newString);
        step.setOperation(TransformationOperation.REPLACE);
        steps.add(step);
    }
    
    /**
     * Adds a reverse step to the transformation description. The source and the target field need to be of String type.
     * Performs standard string reversing on the string of the source field and writes the result to the target field.
     */
    public void reverseField(String sourceField, String targetField) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperation(TransformationOperation.REVERSE);
        steps.add(step);
    }
    
    /**
     * Adds a pad step to the transformation description. The source and the target field need to be of String type.
     * Performs padding operation on the string of the source field and writes the result to the target field. The
     * length describes to which size the padding should be done, the pad character describes which character to use
     * for the padding. The direction describes if the padding should be done at the start or at the end. The standard
     * value for the direction is at the beginning. Values for direction could be "Start" or "End".
     */
    public void padField(String sourceField, String targetField, String length, String character, String direction) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.padLength, length);
        step.setOperationParameter(TransformationConstants.padCharacter, character);
        step.setOperationParameter(TransformationConstants.padDirection, direction);
        step.setOperation(TransformationOperation.PAD);
        steps.add(step);
    }

    public List<TransformationStep> getTransformingSteps() {
        return steps;
    }
}
