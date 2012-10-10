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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Class<?> sourceClass;
    private Class<?> targetClass;
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
            TransformationOperation operation = operationLoader.loadTransformationOperationByName(step.getOperationName());
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
            sources.add(getObjectFromSourceField(sourceField));
        }
        return sources;
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
}
