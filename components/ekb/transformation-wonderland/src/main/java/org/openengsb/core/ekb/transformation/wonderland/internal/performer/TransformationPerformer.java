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

package org.openengsb.core.ekb.transformation.wonderland.internal.performer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.api.transformation.TransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The TransformationPerformer does the actual performing work between objects.
 */
public class TransformationPerformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationPerformer.class);
    private Map<String, Object> temporaryFields;
    private Class<?> sourceClass;
    private Class<?> targetClass;
    private Object source;
    private Object target;
    private ModelRegistry modelRegistry;

    public TransformationPerformer(ModelRegistry modelRegistry) {
        temporaryFields = new HashMap<String, Object>();
        this.modelRegistry = modelRegistry;
    }

    /**
     * Does the checking of all necessary values of the TransformationDescription which are needed to process the
     * description
     */
    private void checkNeededValues(TransformationDescription description) {
        String message = "The TransformationDescription doesn't contain a %s. Description loading aborted";
        if (description.getSourceModel().getModelClassName() == null) {
            throw new IllegalArgumentException(String.format(message, "sourceclass"));
        }
        if (description.getTargetModel().getModelClassName() == null) {
            throw new IllegalArgumentException(String.format(message, "targetclass"));
        }
    }

    /**
     * Transforms the given object based on the given TransformationDescription.
     */
    public Object transformObject(TransformationDescription description, Object source) throws InstantiationException,
        IllegalAccessException, ClassNotFoundException {
        checkNeededValues(description);
        sourceClass = modelRegistry.loadModel(description.getSourceModel());
        targetClass = modelRegistry.loadModel(description.getTargetModel());
        this.source = source;
        target = targetClass.newInstance();
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
            String operation = step.getOperationName();
            if (operation.equals("forward")) {
                performForwardStep(step);
            } else if (operation.equals("concat")) {
                performConcatStep(step);
            } else if (operation.equals("split")) {
                performSplitStep(step);
            } else if (operation.equals("splitRegex")) {
                performSplitRegexStep(step);
            } else if (operation.equals("map")) {
                performMapStep(step);
            } else if (operation.equals("substring")) {
                performSubStringStep(step);
            } else if (operation.equals("value")) {
                performValueStep(step);
            } else if (operation.equals("length")) {
                performLengthStep(step);
            } else if (operation.equals("trim")) {
                performTrimStep(step);
            } else if (operation.equals("toLower")) {
                performToLowerStep(step);
            } else if (operation.equals("toUpper")) {
                performToUpperStep(step);
            } else if (operation.equals("replace")) {
                performReplaceStep(step);
            } else if (operation.equals("reverse")) {
                performReverseStep(step);
            } else if (operation.equals("pad")) {
                performPadStep(step);
            } else if (operation.equals("removeleading")) {
                performRemoveLeadingStep(step);
            } else if (operation.equals("instantiate")) {
                performInstantiationStep(step);
            }
        } catch (TransformationStepException e) {
            LOGGER.debug(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unable to perform transformation step." + step, e);
        }
    }

    /**
     * Logic for a forward transformation step
     */
    private void performForwardStep(TransformationStep step) throws Exception {
        Object value = getObjectFromSourceField(step.getSourceFields()[0]);
        setObjectToTargetField(step.getTargetField(), value);
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
            try {
                builder.append(getObjectFromSourceField(field));
            } catch (TransformationStepException e) {
                // ignore
            }
        }
        setObjectToTargetField(step.getTargetField(), builder.toString());
    }

    /**
     * Logic for a split transformation step
     */
    private void performSplitStep(TransformationStep step) throws Exception {
        String split = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        String splitString = step.getOperationParamater(TransformationConstants.splitParam);
        String indexString = step.getOperationParamater(TransformationConstants.index);
        Integer index = TransformationPerformUtils.parseIntString(indexString, false, 0);
        String[] splits = split.split(splitString);
        String result = "";
        try {
            result = splits[index];
        } catch (IndexOutOfBoundsException e) {
            LOGGER.warn("Split havn't enough results for the given index. The empty string will be taken instead.");
        }
        setObjectToTargetField(step.getTargetField(), result);
    }

    /**
     * Logic for a split regex transformation step
     */
    private void performSplitRegexStep(TransformationStep step) throws Exception {
        String split = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        String splitString = step.getOperationParamater(TransformationConstants.regexParam);
        String indexString = step.getOperationParamater(TransformationConstants.index);
        Integer index = TransformationPerformUtils.parseIntString(indexString, false, 0);
        Matcher matcher = TransformationPerformUtils.generateMatcher(splitString, split);
        for (int i = 0; i <= index; i++) {
            matcher.find();
        }
        String result = matcher.group();
        if (result == null) {
            LOGGER.warn("No result for given regex and index. The empty string will be taken instead.");
            result = "";
        }
        setObjectToTargetField(step.getTargetField(), result);
    }

    /**
     * Logic for a map step
     */
    private void performMapStep(TransformationStep step) throws Exception {
        Object value = getObjectFromSourceField(step.getSourceFields()[0]);
        for (Map.Entry<String, String> entry : step.getOperationParams().entrySet()) {
            if (value.toString().equals(entry.getKey())) {
                value = entry.getValue();
                break;
            }
        }
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for a substring step
     */
    private void performSubStringStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        String fromString = step.getOperationParamater(TransformationConstants.substringFrom);
        String toString = step.getOperationParamater(TransformationConstants.substringTo);
        int from = TransformationPerformUtils.parseIntString(fromString, false, 0);
        int to = TransformationPerformUtils.parseIntString(toString, false, value.length());
        if (to > value.length()) {
            StringBuilder builder = new StringBuilder();
            builder.append("The end index is bigger than the length of the input string. ");
            builder.append("The length of the input string will be taken instead.");
            LOGGER.warn(builder.toString());
            to = value.length();
        }
        value = value.substring(from, to);
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the value step
     */
    private void performValueStep(TransformationStep step) throws Exception {
        Object value = step.getOperationParamater(TransformationConstants.value);
        if (value == null) {
            LOGGER.warn("There was no value set for the value step. This step will be skipped.");
            return;
        }
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the length step
     */
    private void performLengthStep(TransformationStep step) throws Exception {
        Object value = getObjectFromSourceField(step.getSourceFields()[0]);
        String length = "0";
        String function = step.getOperationParamater(TransformationConstants.lengthFunction);
        try {
            function = function != null ? function : "length";
            Method method = value.getClass().getMethod(function);
            length = method.invoke(value).toString();
        } catch (NoSuchMethodException e) {
            StringBuilder builder = new StringBuilder();
            builder.append("The type of the given field for the length step doesn't support ");
            builder.append(function).append(" method. So 0 will be used as standard value.");
            LOGGER.warn(builder.toString());
        }
        setObjectToTargetField(step.getTargetField(), length);
    }

    /**
     * Logic for the trim step
     */
    private void performTrimStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        value = value.trim();
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the toUpper step
     */
    private void performToUpperStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        value = value.toUpperCase();
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the toLower step
     */
    private void performToLowerStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        value = value.toLowerCase();
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the replace step
     */
    private void performReplaceStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        String oldString = step.getOperationParamater(TransformationConstants.replaceOld);
        String newString = step.getOperationParamater(TransformationConstants.replaceNew);
        if (oldString == null || newString == null) {
            String message = "The replace step from %s to %s isn't complete defined. The step will be skipped.";
            LOGGER.warn(String.format(message, step.getSourceFields()[0], step.getTargetField()));
        }
        value = StringUtils.replace(value, oldString, newString);
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the reverse step
     */
    private void performReverseStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        value = StringUtils.reverse(value);
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the pad step
     */
    private void performPadStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        String lengthString = step.getOperationParamater(TransformationConstants.padLength);
        String characterString = step.getOperationParamater(TransformationConstants.padCharacter);
        String directionString = step.getOperationParamater(TransformationConstants.padDirection);
        Integer length = TransformationPerformUtils.parseIntString(lengthString, true, 0);
        if (characterString == null || characterString.isEmpty()) {
            String message = "The given character string for the pad is empty. Step will be skipped.";
            LOGGER.error(message);
            throw new TransformationStepException(message);
        }
        char character = characterString.charAt(0);
        if (characterString.length() > 0) {
            LOGGER.debug("The given character string is longer than one element. The first character is used.");
        }
        if (directionString == null || !(directionString.equals("Start") || directionString.equals("End"))) {
            LOGGER.debug("Unrecognized direction string. The standard value 'Start' will be used.");
            directionString = "Start";
        }

        if (directionString.equals("Start")) {
            value = Strings.padStart(value, length, character);
        } else {
            value = Strings.padEnd(value, length, character);
        }
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the remove leading step
     */
    private void performRemoveLeadingStep(TransformationStep step) throws Exception {
        String value = getTypedObjectFromSourceField(step.getSourceFields()[0], String.class);
        String regex = step.getOperationParamater(TransformationConstants.regexParam);
        String lengthString = step.getOperationParamater(TransformationConstants.removeLeadingLength);
        Integer length = TransformationPerformUtils.parseIntString(lengthString, false, 0);
        Matcher matcher = TransformationPerformUtils.generateMatcher(regex, value);
        if (length != null && length != 0) {
            matcher.region(0, length);
        }
        if (matcher.find()) {
            String matched = matcher.group();
            value = value.substring(matched.length());
        }
        setObjectToTargetField(step.getTargetField(), value);
    }

    /**
     * Logic for the instantiate step
     */
    private void performInstantiationStep(TransformationStep step) throws Exception {
        Object sourceObject = getObjectFromSourceField(step.getSourceFields()[0]);
        String targetType = step.getOperationParamater(TransformationConstants.instantiateTargetType);
        String targetTypeInit = step.getOperationParamater(TransformationConstants.instantiateInitMethod);
        Object targetObject = null;
        Class<?> targetClass = null;
        try {
            targetClass = this.getClass().getClassLoader().loadClass(targetType);
        } catch (Exception e) {
            String message = "The class %s can't be found. The instantiate step will be ignored.";
            message = String.format(message, targetType);
            LOGGER.error(message);
            throw new TransformationStepException(message, e);
        }
        try {
            if (targetTypeInit == null) {
                Constructor<?> constr = targetClass.getConstructor(sourceObject.getClass());
                targetObject = constr.newInstance(sourceObject);
            } else {
                Method method = targetClass.getMethod(targetTypeInit, sourceObject.getClass());
                if (Modifier.isStatic(method.getModifiers())) {
                    targetObject = method.invoke(null, sourceObject);
                } else {
                    targetObject = method.invoke(targetClass.newInstance(), sourceObject);
                }
            }
        } catch (Exception e) {
            String message = "Unable to create the desired object. The instantiate step will be ignored.";
            message = String.format(message, targetType);
            LOGGER.error(message);
            throw new TransformationStepException(message, e);
        }
        setObjectToTargetField(step.getTargetField(), targetObject);
    }

    /**
     * Sets the given value object to the field with the field name of the target object. Is also aware of temporary
     * fields.
     */
    private void setObjectToTargetField(String fieldname, Object value) throws Exception {
        if (TransformationPerformUtils.isTemporaryField(fieldname)) {
            temporaryFields.put(fieldname, value);
        } else {
            Method setter = getSetterForField(fieldname, value);
            if (setter == null) {
                String message = "There is no setter for the field %s at the target model found. Step will be skipped";
                LOGGER.error(String.format(message, fieldname));
                return;
            }
            setter.invoke(target, value);
        }
    }

    /**
     * Tries to get the setter for the given field name. May need a search for the method in all methods the target
     * model has.
     */
    private Method getSetterForField(String fieldname, Object value) throws Exception {
        String methodName = TransformationPerformUtils.getSetterName(fieldname);
        Method setter = null;
        if (value == null) {
            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    setter = method;
                    break;
                }
            }
        } else {
            setter = targetClass.getMethod(methodName, value.getClass());
        }
        return setter;
    }

    /**
     * Gets the value of the field with the field name of the source object. Is also aware of temporary fields.
     */
    private Object getObjectFromSourceField(String fieldname) throws Exception {
        if (TransformationPerformUtils.isTemporaryField(fieldname)) {
            if (!temporaryFields.containsKey(fieldname)) {
                String message = String.format("The temporary field %s doesn't exist.", fieldname);
                throw new TransformationStepException(message);
            }
            Object temp = temporaryFields.get(fieldname);
            return temp;
        } else {
            Method getter = sourceClass.getMethod(TransformationPerformUtils.getGetterName(fieldname));
            Object result = getter.invoke(source);
            if (result == null) {
                String message = String.format("The source field %s is null and can be ignored", fieldname);
                throw new TransformationStepException(message);
            }
            return result;
        }
    }

    /**
     * Gets the value of the field with the field name of the source object and try to type it.
     */
    @SuppressWarnings("unchecked")
    private <T> T getTypedObjectFromSourceField(String fieldname, Class<T> type) throws Exception {
        Object result = getObjectFromSourceField(fieldname);
        if (result.getClass().equals(type)) {
            return (T) result;
        }
        String message = String.format("The field %s hasn't the type %s.", fieldname, type.getName());
        throw new TransformationStepException(message);
    }

}
