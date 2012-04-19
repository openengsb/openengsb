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

package org.openengsb.core.ekb.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.ekb.TransformationConstants;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.openengsb.core.api.ekb.transformation.TransformationStep;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TransformationPerformer does the actual performing work between objects.
 */
public class TransformationPerformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationPerformer.class);
    private Map<String, Object> temporaryFields;
    private TransformationDescription description;
    private Object source;
    private Object target;

    public TransformationPerformer() {
        temporaryFields = new HashMap<String, Object>();
    }

    /**
     * Transforms the given object based on the given TransformationDescription.
     */
    public Object transformObject(TransformationDescription description, Object source) throws InstantiationException,
        IllegalAccessException {
        this.description = description;
        this.source = source;
        if (OpenEngSBModel.class.isAssignableFrom(description.getTarget())) {
            target = ModelUtils.createModelObject(description.getTarget());
        } else {
            target = description.getTarget().newInstance();
        }

        for (TransformationStep step : description.getTransformingSteps()) {
            performTransformationStep(step);
        }
        return target;
    }

    /**
     * Performs one transformation step
     */
    private void performTransformationStep(TransformationStep step) throws IllegalAccessException {
        try {
            switch (step.getOperation()) {
                case FORWARD:
                    performForwardStep(step);
                    break;
                case CONCAT:
                    performConcatStep(step);
                    break;
                case SPLIT:
                    performSplitStep(step);
                    break;
                case MAP:
                    performMapStep(step);
                    break;
                case SUBSTRING:
                    performSubStringStep(step);
                    break;
                default:
                    LOGGER.error("Unsupported operation: " + step.getOperation());
            }
        } catch (Exception e) {
            LOGGER.error("Unable to perform transformation step ." + step, e);
        }
    }

    /**
     * Logic for a forward transformation step
     */
    private void performForwardStep(TransformationStep step) throws Exception {
        Object value = getObjectFromField(step.getSourceFields()[0], description.getSource(), source);
        setObjectToField(step.getTargetField(), description.getTarget(), target, value);
    }

    /**
     * Logic for a concat transformation step
     */
    private void performConcatStep(TransformationStep step) throws Exception {
        StringBuilder builder = new StringBuilder();
        String concatString = step.getOperationParamater(TransformationConstants.concatParam);
        for (String field : step.getSourceFields()) {
            if (builder.length() != 0) {
                builder.append(concatString);
            }
            builder.append(getObjectFromField(field, description.getSource(), source));
        }
        setObjectToField(step.getTargetField(), description.getTarget(), target, builder.toString());
    }

    /**
     * Logic for a split transformation step
     */
    private void performSplitStep(TransformationStep step) throws Exception {
        String split = (String) getObjectFromField(step.getSourceFields()[0], description.getSource(), source);
        String splitString = step.getOperationParamater(TransformationConstants.splitParam);
        Integer index = 0;
        try {
            index = Integer.parseInt(step.getOperationParamater(TransformationConstants.index));
        } catch (NumberFormatException e) {
            System.out.println(step.getOperationParamater(TransformationConstants.index));
            LOGGER.warn("The index given for the split operation is not a number. 0 will be taken instead");
        }
        String[] splits = split.split(splitString);
        String result = "";
        try {
            result = splits[index];
        } catch (IndexOutOfBoundsException e) {
            LOGGER.warn("Split havn't enough results for the given index. The empty string will be taken instead");
        }
        setObjectToField(step.getTargetField(), description.getTarget(), target, result);
    }

    /**
     * Logic for a map step
     */
    private void performMapStep(TransformationStep step) throws Exception {
        Object value = getObjectFromField(step.getSourceFields()[0], description.getSource(), source);
        for (Map.Entry<String, String> entry : step.getOperationParams().entrySet()) {
            if (value.toString().equals(entry.getKey())) {
                value = entry.getValue();
                break;
            }
        }
        setObjectToField(step.getTargetField(), description.getTarget(), target, value);
    }

    /**
     * Logic for a substring step
     */
    private void performSubStringStep(TransformationStep step) throws Exception {
        String value = (String) getObjectFromField(step.getSourceFields()[0], description.getSource(), source);
        String fromString = step.getOperationParamater(TransformationConstants.substringFrom);
        String toString = step.getOperationParamater(TransformationConstants.substringTo);
        int from = 0;
        int to = value.length();
        if (fromString != null) {
            try {
                from = Integer.parseInt(fromString);
            } catch (NumberFormatException e) {
                LOGGER.warn("The String defining the start index of the substring is no number. "
                        + "0 will be taken instead.");
            }
        }
        if (toString != null) {
            try {
                to = Integer.parseInt(toString);
            } catch (NumberFormatException e) {
                LOGGER.warn("The String defining the end index of the substring is no number. "
                        + "The length of input string will be taken instead.");
            }
        }
        if (to > value.length()) {
            LOGGER.warn("The end index is bigger than the length of the input string. "
                    + "The length of the input string will be taken instead.");
            to = value.length();
        }
        value = value.substring(from, to);
        setObjectToField(step.getTargetField(), description.getTarget(), target, value);
    }

    /**
     * Sets the given value object to the field with the fieldname of the given target object with the class of the
     * target object. Is also aware of temporary fields.
     */
    private void setObjectToField(String fieldname, Class<?> clazz, Object target, Object value) throws Exception {
        if (isTemporaryField(fieldname)) {
            temporaryFields.put(fieldname, value);
        } else {
            Method setter = clazz.getMethod(getSetterName(fieldname), value.getClass());
            setter.invoke(target, value);
        }
    }

    /**
     * Gets the value of the field with the fieldname of the given source object with the class of the source object. Is
     * also aware of temporary fields.
     */
    private Object getObjectFromField(String fieldname, Class<?> clazz, Object source) throws Exception {
        if (isTemporaryField(fieldname)) {
            Object temp = temporaryFields.get(fieldname);
            return temp;
        } else {
            Method getter = clazz.getMethod(getGetterName(fieldname));
            return getter.invoke(source);
        }
    }

    /**
     * Returns true if the given fieldname points to a temporary field. Returns false if not.
     */
    private boolean isTemporaryField(String fieldname) {
        return fieldname.startsWith("temp.");
    }

    /**
     * Returns the name of the getter method of a field.
     */
    private String getGetterName(String fieldname) {
        return "get" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }

    /**
     * Returns the name of the setter method of a field.
     */
    private String getSetterName(String fieldname) {
        return "set" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }
}
