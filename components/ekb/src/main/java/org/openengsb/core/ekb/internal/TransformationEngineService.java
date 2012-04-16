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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.ekb.TransformationEngine;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.openengsb.core.api.ekb.transformation.TransformationStep;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Implementation of the transformation engine. Only supports the transformations from OpenEngSBModels to
 * OpenEngSBModels.
 */
public class TransformationEngineService implements TransformationEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationEngineService.class);

    private List<TransformationDescription> descriptions;

    public TransformationEngineService() {
        descriptions = new ArrayList<TransformationDescription>();
    }

    @Override
    public void saveDescription(TransformationDescription description) {
        deleteDescription(description);
        descriptions.add(description);
    }

    @Override
    public void deleteDescription(TransformationDescription description) {
        for (TransformationDescription desc : descriptions) {
            if (desc.getSource().equals(description.getSource()) && desc.getTarget().equals(description.getTarget())) {
                descriptions.remove(desc);
                return;
            }
        }
    }

    @Override
    public void addDescriptionsFromFile(File file) {
        List<TransformationDescription> descriptions = getDescriptionsFromFile(file);
        for (TransformationDescription description : descriptions) {
            saveDescription(description);
        }
    }

    @Override
    public void addDescriptionsFromInputStream(InputStream inputStream) {
        List<TransformationDescription> descriptions = getDescriptionsFromInputStream(inputStream);
        for (TransformationDescription description : descriptions) {
            saveDescription(description);
        }
    }

    @Override
    public List<TransformationDescription> getDescriptionsFromInputStream(InputStream fileContent) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            desc = loadFromInputSource(new InputSource(fileContent));
        } catch (Exception e) {
            LOGGER.error("Unable to read the descriptions from input stream. ", e);
        }
        return desc;
    }

    @Override
    public List<TransformationDescription> getDescriptionsFromFile(File file) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            return loadFromInputSource(new InputSource(file.getAbsolutePath()));
        } catch (Exception e) {
            LOGGER.error("Unable to read the descriptions from file " + file.getAbsolutePath(), e);
        }
        return desc;
    }

    private List<TransformationDescription> loadFromInputSource(InputSource source) throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        TransformationDescriptionXMLReader reader = new TransformationDescriptionXMLReader();
        xr.setContentHandler(reader);
        xr.parse(source);
        return reader.getResult();
    }

    @SuppressWarnings("unchecked")
    public <T> T performTransformation(Class<?> sourceClass, Class<T> targetClass, Object source) {
        try {
            TransformationDescription desc = null;
            for (TransformationDescription td : descriptions) {
                if (td.getSource().equals(sourceClass) && td.getTarget().equals(targetClass)) {
                    desc = td;
                    break;
                }
            }
            if (desc != null) {
                return (T) doActualTransformationSteps(desc, source);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("No transformation description for this class pair defined");
    }

    /**
     * Performs the actual transformation work. Performs step-wise the transformation steps.
     */
    private Object doActualTransformationSteps(TransformationDescription td, Object source)
        throws InstantiationException, IllegalAccessException {
        Object result;
        if (OpenEngSBModel.class.isAssignableFrom(td.getTarget())) {
            result = ModelUtils.createModelObject(td.getTarget());
        } else {
            result = td.getTarget().newInstance();
        }

        for (TransformationStep step : td.getTransformingSteps()) {
            performTransformationStep(step, td, source, result);
        }
        return result;
    }

    /**
     * Performs one transformation step
     */
    private void performTransformationStep(TransformationStep step, TransformationDescription desc, Object source,
            Object target) throws IllegalAccessException {
        try {
            switch (step.getOperation()) {
                case FORWARD:
                    performForwardStep(desc, step, source, target);
                    break;
                case CONCAT:
                    performConcatStep(desc, step, source, target);
                    break;
                case SPLIT:
                    performSplitStep(desc, step, source, target);
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
    private void performForwardStep(TransformationDescription desc, TransformationStep step, Object source,
            Object target) throws Exception {
        Method getter = desc.getSource().getMethod(getGetterName(step.getSourceFields()[0]));
        Object object = getter.invoke(source);
        Method setter = desc.getTarget().getMethod(getSetterName(step.getTargetField()), object.getClass());
        setter.invoke(target, object);
    }

    /**
     * Logic for a concat transformation step
     */
    private void performConcatStep(TransformationDescription desc, TransformationStep step, Object source,
            Object target) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (String field : step.getSourceFields()) {
            if (builder.length() != 0) {
                builder.append(step.getOperationParam());
            }
            Method getter = desc.getSource().getMethod(getGetterName(field));
            builder.append(getter.invoke(source));
        }
        Method setter = desc.getTarget().getMethod(getSetterName(step.getTargetField()), String.class);
        setter.invoke(target, builder.toString());
    }

    /**
     * Logic for a split transformation step
     */
    private void performSplitStep(TransformationDescription desc, TransformationStep step, Object source,
            Object target) throws Exception {
        Method getter = desc.getSource().getMethod(getGetterName(step.getTargetField()));
        String split = (String) getter.invoke(source);
        String[] splits = split.split(step.getOperationParam());
        for (int i = 0; i < step.getSourceFields().length; i++) {
            if (splits.length <= i) {
                LOGGER.warn("Not enough results of the split operation for the given target fields.");
                break;
            }
            String field = step.getSourceFields()[i];
            Method setter = desc.getTarget().getMethod(getSetterName(field), String.class);
            setter.invoke(target, splits[i]);
        }
        if (splits.length > step.getSourceFields().length) {
            LOGGER.warn("Too many results of the split operation for the given target fields. "
                    + "Data will get lost!");
        }
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
