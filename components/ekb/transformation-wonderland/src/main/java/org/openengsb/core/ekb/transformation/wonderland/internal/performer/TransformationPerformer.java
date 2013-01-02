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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationLoader;
import org.openengsb.core.ekb.api.transformation.TransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TransformationPerformer does the actual performing work between objects.
 */
public class TransformationPerformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationPerformer.class);
    private Map<String, Object> temporaryFields;
    private Object source;
    private Object target;
    private ModelRegistry modelRegistry;
    private TransformationOperationLoader operationLoader;

    public TransformationPerformer(ModelRegistry modelRegistry, TransformationOperationLoader operationLoader) {
        temporaryFields = new HashMap<String, Object>();
        this.modelRegistry = modelRegistry;
        this.operationLoader = operationLoader;
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
        return transformObject(description, source, null);
    }

    /**
     * Performs a transformation based merge of the given source object with the given target object based on the given
     * TransformationDescription.
     */
    public Object transformObject(TransformationDescription description, Object source, Object target)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        checkNeededValues(description);
        Class<?> sourceClass = modelRegistry.loadModel(description.getSourceModel());
        Class<?> targetClass = modelRegistry.loadModel(description.getTargetModel());
        if (!sourceClass.isAssignableFrom(source.getClass())) {
            throw new IllegalArgumentException("The given source object does not match the given description");
        }
        this.source = source;
        if (target == null) {
            this.target = targetClass.newInstance();
        } else {
            this.target = target;
        }
        for (TransformationStep step : description.getTransformingSteps()) {
            performTransformationStep(step);
        }
        return this.target;
    }

    /**
     * Performs one transformation step
     */
    private void performTransformationStep(TransformationStep step) throws IllegalAccessException {
        try {
            TransformationOperation operation =
                operationLoader.loadTransformationOperationByName(step.getOperationName());
            Object value = operation.performOperation(getSourceFieldValues(step), step.getOperationParams());
            setObjectToTargetField(step.getTargetField(), value);
        } catch (TransformationStepException e) {
            LOGGER.debug(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unable to perform transformation step {}.", step, e);
        }
    }

    /**
     * Returns a list of actual field values from the sources of the given transformation step
     */
    private List<Object> getSourceFieldValues(TransformationStep step) throws Exception {
        List<Object> sources = new ArrayList<Object>();
        for (String sourceField : step.getSourceFields()) {
            Object object = getObjectValue(sourceField, true);
            if (object == null) {
                String message = String.format("The source field %s is null. Step will be be ignored", sourceField);
                throw new TransformationStepException(message);
            }
            sources.add(object);
        }
        return sources;
    }

    /**
     * Sets the given value object to the field with the field name of the target object. Is also aware of temporary
     * fields.
     */
    private void setObjectToTargetField(String fieldname, Object value) throws Exception {
        Object toWrite = null;
        if (fieldname.contains(".")) {
            String path = StringUtils.substringBeforeLast(fieldname, ".");
            toWrite = getObjectValue(path, false);
        }
        if (toWrite == null && isTemporaryField(fieldname)) {
            String mapKey = StringUtils.substringAfter(fieldname, "#");
            mapKey = StringUtils.substringBefore(mapKey, ".");
            temporaryFields.put(mapKey, value);
            return;
        }
        String realFieldName = fieldname.contains(".") ? StringUtils.substringAfterLast(fieldname, ".") : fieldname;
        writeObjectToField(realFieldName, value, toWrite, target);
    }

    /**
     * Gets the value of the field with the field name either from the source object or the target object, depending on
     * the parameters. Is also aware of temporary fields.
     */
    private Object getObjectValue(String fieldname, boolean fromSource) throws Exception {
        Object sourceObject = fromSource ? source : target;
        Object result = null;
        for (String part : StringUtils.split(fieldname, ".")) {
            if (isTemporaryField(part)) {
                result = loadObjectFromTemporary(part, fieldname);
            } else {
                result = loadObjectFromField(part, result, sourceObject);
            }
        }
        return result;
    }

    /**
     * Loads the object for the given field name from the temporary fields. If an error occurs, the complete path
     * (inclusive nested names) are given in an exception.
     */
    private Object loadObjectFromTemporary(String fieldname, String complete) throws Exception {
        String realName = fieldname.substring(1);
        if (!temporaryFields.containsKey(realName)) {
            String message = String.format("The temporary field %s doesn't exist.", complete);
            throw new IllegalArgumentException(message);
        }
        return temporaryFields.get(realName);
    }

    /**
     * Loads the object from the field with the given name from either the object parameter or if this parameter is null
     * from the alternative parameter.
     */
    private Object loadObjectFromField(String fieldname, Object object, Object alternative) throws Exception {
        Object source = object != null ? object : alternative;
        try {
            return FieldUtils.readField(source, fieldname, true);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unable to load field '%s' from object '%s'", fieldname,
                source.getClass().getName()));
        }
    }

    /**
     * Writes a value to the object from the field with the given name from either the object parameter or if this
     * parameter is null from the alternative parameter.
     */
    private void writeObjectToField(String fieldname, Object value, Object object, Object alternative)
        throws Exception {
        Object target = object != null ? object : alternative;
        try {
            FieldUtils.writeField(target, fieldname, value, true);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unable to write value '%s' to field '%s' of object %s",
                value.toString(), fieldname, target.getClass().getName()));
        }
    }

    /**
     * Returns true if the given field name points to a temporary field. Returns false if not.
     */
    private boolean isTemporaryField(String fieldname) {
        return fieldname.startsWith("#");
    }
}
