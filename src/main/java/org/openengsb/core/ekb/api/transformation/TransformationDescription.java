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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.api.model.ModelDescription;

import com.google.common.base.Objects;

/**
 * Describes all steps to transform an object of the source class into an object of the target class.
 */
public class TransformationDescription {
    private ModelDescription sourceModel;
    private ModelDescription targetModel;
    private List<TransformationStep> steps;
    private Map<String, Set<String>> propertyConnections;
    private String fileName;
    private String id;

    public TransformationDescription(ModelDescription sourceModel, ModelDescription targetModel) {
        this(sourceModel, targetModel, null);
    }
    
    public TransformationDescription(ModelDescription sourceModel, ModelDescription targetModel, String id) {
        this.sourceModel = sourceModel;
        this.targetModel = targetModel;
        this.id = id;
        steps = new ArrayList<TransformationStep>();
    }

    /**
     * Adds a transformation step to this description
     */
    public void addStep(String operationName, List<String> sourceFields, String targetField,
            Map<String, String> parameters) {
        TransformationStep step = new TransformationStep();
        step.setOperationName(operationName);
        step.setSourceFields(sourceFields.toArray(new String[sourceFields.size()]));
        step.setTargetField(targetField);
        step.setOperationParams(parameters);
        steps.add(step);
    }

    /**
     * Adds a forward transformation step to the transformation description. The value of the source field is copied to
     * the target field unchanged. Both fields need to have the same object type.
     */
    public void forwardField(String sourceField, String targetField) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperationName("forward");
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
        step.setOperationParameter(TransformationConstants.CONCAT_PARAM, concatString);
        step.setOperationName("concat");
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
        step.setOperationParameter(TransformationConstants.SPLIT_PARAM, splitString);
        step.setOperationParameter(TransformationConstants.INDEX_PARAM, index);
        step.setOperationName("split");
        steps.add(step);
    }

    /**
     * Adds a split regex transformation step to the transformation description. The value of the source field is split
     * based on the split string as regular expression into parts. Based on the given index, the result will be set to
     * the target field. The index needs to be an integer value. All fields need to be of the type String.
     */
    public void splitRegexField(String sourceField, String targetField, String regexString, String index) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setSourceFields(sourceField);
        step.setOperationParameter(TransformationConstants.REGEX_PARAM, regexString);
        step.setOperationParameter(TransformationConstants.INDEX_PARAM, index);
        step.setOperationName("splitRegex");
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
        step.setOperationName("map");
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
        step.setOperationParameter(TransformationConstants.SUBSTRING_FROM_PARAM, from);
        step.setOperationParameter(TransformationConstants.SUBSTRING_TO_PARAM, to);
        step.setOperationName("substring");
        steps.add(step);
    }

    /**
     * Adds a value step to the transformation description. The given value is written to the target field.
     */
    public void valueField(String targetField, String value) {
        TransformationStep step = new TransformationStep();
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.VALUE_PARAM, value);
        step.setOperationName("value");
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
        step.setOperationParameter(TransformationConstants.LENGTH_FUNCTION_PARAM, function);
        step.setOperationName("length");
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
        step.setOperationName("trim");
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
        step.setOperationName("toLower");
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
        step.setOperationName("toUpper");
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
        step.setOperationParameter(TransformationConstants.REPLACE_OLD_PARAM, oldString);
        step.setOperationParameter(TransformationConstants.REPLACE_NEW_PARAM, newString);
        step.setOperationName("replace");
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
        step.setOperationName("reverse");
        steps.add(step);
    }

    /**
     * Adds a pad step to the transformation description. The source and the target field need to be of String type.
     * Performs padding operation on the string of the source field and writes the result to the target field. The
     * length describes to which size the padding should be done, the pad character describes which character to use for
     * the padding. The direction describes if the padding should be done at the start or at the end. The standard value
     * for the direction is at the beginning. Values for direction could be "Start" or "End".
     */
    public void padField(String sourceField, String targetField, String length, String character, String direction) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.PAD_LENGTH_PARAM, length);
        step.setOperationParameter(TransformationConstants.PAD_CHARACTER_PARAM, character);
        step.setOperationParameter(TransformationConstants.PAD_DIRECTION_PARAM, direction);
        step.setOperationName("pad");
        steps.add(step);
    }

    /**
     * Adds a remove leading step to the transformation description. The source and the target field need to be of
     * String type. Based on the given regular expression string the beginning of the source string will be removed
     * until the given maximum length. If the length is 0, the maximum length is ignored.
     */
    public void removeLeadingField(String sourceField, String targetField, String regexString, String length) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.REMOVELEADING_LENGTH_PARAM, length);
        step.setOperationParameter(TransformationConstants.REGEX_PARAM, regexString);
        step.setOperationName("removeleading");
        steps.add(step);
    }

    /**
     * Adds an instantiate step to the transformation description. An object defined through the given target type will
     * be created. For the instantiation the targetTypeInit method will be used. If this parameter is null, the
     * constructor of the targetType will be used with the object type of the source object as parameter.
     */
    public void instantiateField(String sourceField, String targetField, String targetType, String targetTypeInit) {
        TransformationStep step = new TransformationStep();
        step.setSourceFields(sourceField);
        step.setTargetField(targetField);
        step.setOperationParameter(TransformationConstants.INSTANTIATE_TARGETTYPE_PARAM, targetType);
        step.setOperationParameter(TransformationConstants.INSTANTIATE_INITMETHOD_PARAM, targetTypeInit);
        step.setOperationName("instantiate");
        steps.add(step);
    }

    public ModelDescription getSourceModel() {
        return sourceModel;
    }

    public ModelDescription getTargetModel() {
        return targetModel;
    }

    public List<TransformationStep> getTransformingSteps() {
        return steps;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Map<String, Set<String>> getPropertyConnections() {
        return propertyConnections;
    }
    
    public void setPropertyConnections(Map<String, Set<String>> propertyConnections) {
        this.propertyConnections = propertyConnections;
    }
    
    @Override
    public int hashCode() {
        Object []obj = new Object[] { sourceModel, steps, targetModel, id};
        return Objects.hashCode(obj);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(TransformationDescription.class)) {
            return false;
        }
        TransformationDescription other = (TransformationDescription) obj;
        boolean sourceEqual = Objects.equal(sourceModel, other.getSourceModel());
        boolean targetEqual = Objects.equal(targetModel, other.getTargetModel());
        boolean idEqual = Objects.equal(id, other.getId());
        return sourceEqual && targetEqual && idEqual;
    }
}
