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
import java.lang.reflect.InvocationTargetException;
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
    public List<TransformationDescription> getDescriptionsFromFile(File file) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            TransformationDescriptionXMLReader reader = new TransformationDescriptionXMLReader();
            xr.setContentHandler(reader);
            xr.parse(file.getAbsolutePath());
            desc = reader.getResult();
        } catch (Exception e) {
            LOGGER.error("unable to read the descriptions from file " + file.getAbsolutePath(), e);
        }
        return desc;
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
        Method getter;
        Method setter;
        Object object;
        for (TransformationStep step : td.getTransformingSteps()) {
            try {
                switch (step.getOperation()) {
                    case FORWARD:
                        getter = td.getSource().getMethod(getGetterName(step.getSourceFields()[0]));
                        object = getter.invoke(source);
                        setter =
                            td.getTarget().getMethod(getSetterName(step.getTargetField()), object.getClass());
                        setter.invoke(result, object);
                        break;
                    case CONCAT:
                        StringBuilder builder = new StringBuilder();
                        for (String field : step.getSourceFields()) {
                            if (builder.length() != 0) {
                                builder.append(step.getOperationParam());
                            }
                            getter = td.getSource().getMethod(getGetterName(field));
                            builder.append(getter.invoke(source));
                        }
                        setter = td.getTarget().getMethod(getSetterName(step.getTargetField()), String.class);
                        setter.invoke(result, builder.toString());
                        break;
                    case SPLIT:
                        getter = td.getSource().getMethod(getGetterName(step.getTargetField()));
                        String split = (String) getter.invoke(source);
                        String[] splits = split.split(step.getOperationParam());
                        for (int i = 0; i < step.getSourceFields().length; i++) {
                            if (splits.length <= i) {
                                LOGGER.warn("Not enough results of the split operation for the given target fields.");
                                break;
                            }
                            String field = step.getSourceFields()[i];
                            setter = td.getTarget().getMethod(getSetterName(field), String.class);
                            setter.invoke(result, splits[i]);
                        }
                        if (splits.length > step.getSourceFields().length) {
                            LOGGER
                                .warn("Too many results of the split operation for the given target fields. "
                                        + "Data will get lost!");
                        }
                        break;
                    default:
                        LOGGER.error("Unsupported operation: " + step.getOperation());

                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
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
