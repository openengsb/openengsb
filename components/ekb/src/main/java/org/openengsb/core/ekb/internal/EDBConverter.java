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

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.model.FileWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.common.model.FileConverterStep;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EDBConverter class responsibility is the converting between EDBObjects and models and the vice-versa.
 */
public class EDBConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EDBConverter.class);
    private EngineeringDatabaseService edbService;

    /**
     * Converts an EDBObject to a model of the given model type.
     */
    @SuppressWarnings("unchecked")
    public <T extends OpenEngSBModel> T convertEDBObjectToModel(Class<T> model, EDBObject object) {
        return (T) convertEDBObjectToUncheckedModel(model, object);
    }

    /**
     * Converts a list of EDBObjects to a list of models of the given model type.
     */
    public <T extends OpenEngSBModel> List<T> convertEDBObjectsToModelObjects(Class<T> model,
            List<EDBObject> objects) {
        List<T> models = new ArrayList<T>();
        for (EDBObject object : objects) {
            T instance = convertEDBObjectToModel(model, object);
            if (instance != null) {
                models.add(instance);
            }
        }
        return models;
    }

    /**
     * Creates an instance of the given class object. If it is an OpenEngSBModel, a model is created, else this method
     * tries to generate a new instance by calling the standard constructor.
     */
    private Object createNewInstance(Class<?> model) {
        if (model.isInterface() && OpenEngSBModel.class.isAssignableFrom(model)) {
            return ModelUtils.createModelObject(model);
        } else {
            try {
                return model.newInstance();
            } catch (InstantiationException e) {
                LOGGER.error("instantiation exception while trying to create instance of class {}", model.getName());
            } catch (IllegalAccessException e) {
                LOGGER.error("illegal access exception while trying to create instance of class {}", model.getName());
            }
        }
        return null;
    }

    /**
     * Converts an EDBObject to a model by analyzing the object and trying to call the corresponding setters of the
     * model.
     */
    private Object convertEDBObjectToUncheckedModel(Class<?> model, EDBObject object) {
        Object instance = createNewInstance(model);
        boolean nothingSet = true;

        for (PropertyDescriptor propertyDescriptor : ModelUtils.getPropertyDescriptorsForClass(model)) {
            if (propertyDescriptor.getWriteMethod() == null) {
                continue;
            }
            Method setterMethod = propertyDescriptor.getWriteMethod();
            Object value = getValueForProperty(propertyDescriptor, object);
            if (value != null) {
                invokeSetterMethod(setterMethod, instance, value);
                nothingSet = false;
            }
        }

        if (nothingSet) {
            return null;
        } else {
            return instance;
        }
    }

    /**
     * Invokes a setter method of the given object with the given parameter.
     */
    private void invokeSetterMethod(Method setterMethod, Object instance, Object parameter) {
        try {
            setterMethod.invoke(instance, parameter);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("illegal argument exception when invoking {} with argument {}",
                setterMethod.getName(), parameter);
        } catch (IllegalAccessException ex) {
            LOGGER.error("illegal access exception when invoking {} with argument {}",
                setterMethod.getName(), parameter);
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            LOGGER.error("invocatin target exception when invoking {} with argument {}",
                setterMethod.getName(), parameter);
        }
    }

    /**
     * Generate the value for a specific property of a model out of an EDBObject.
     */
    private Object getValueForProperty(PropertyDescriptor propertyDescriptor, EDBObject object) {
        Method setterMethod = propertyDescriptor.getWriteMethod();
        String propertyName = propertyDescriptor.getName();
        Object value = object.get(propertyName);
        Class<?> parameterType = setterMethod.getParameterTypes()[0];

        if (object.containsKey(propertyName + "0")) {
            Class<?> clazz = getGenericParameterClass(setterMethod);
            value = getListValue(clazz, propertyName, object);
        } else if (OpenEngSBModel.class.isAssignableFrom(parameterType)) {
            value = convertEDBObjectToUncheckedModel(parameterType, edbService.getObject((String) value));
        } else if (parameterType.equals(File.class)) {
            FileWrapper wrapper = new FileWrapper();
            String filename = (String) object.get(propertyName + ".filename");
            String content = (String) value;
            wrapper.setFilename(filename);
            wrapper.setContent(Base64.decodeBase64(content));
            value = FileConverterStep.getInstance().convertForGetter(wrapper);
        } else if (object.containsKey(propertyName)) {
            if (parameterType.isEnum()) {
                value = getEnumValue(parameterType, value);
            }
        }

        return value;
    }

    /**
     * Get the type of the parameter of a setter.
     */
    private Class<?> getGenericParameterClass(Method setterMethod) {
        Type t = setterMethod.getGenericParameterTypes()[0];
        ParameterizedType pType = (ParameterizedType) t;
        Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
        return clazz;
    }

    /**
     * Gets a list object out of an EDBObject.
     */
    private Object getListValue(Class<?> type, String propertyName, EDBObject object) {
        List<Object> temp = new ArrayList<Object>();
        for (int i = 0;; i++) {
            Object obj;

            if (!object.containsKey(propertyName + i)) {
                break;
            }

            if (OpenEngSBModel.class.isAssignableFrom(type)) {
                obj = convertEDBObjectToUncheckedModel(type, edbService.getObject(object.getString(propertyName + i)));
            } else {
                obj = object.get(propertyName + i);
            }

            temp.add(obj);
        }
        return temp;
    }

    /**
     * Gets an enum value out of an object.
     */
    private Object getEnumValue(Class<?> type, Object value) {
        Object[] enumValues = type.getEnumConstants();
        for (Object enumValue : enumValues) {
            if (enumValue.toString().equals(value.toString())) {
                value = enumValue;
                break;
            }
        }
        return value;
    }

    /**
     * Convert a list of models to a list of EDBObjects (the version retrieving is not considered here. This is done in
     * the EDB directly).
     */
    public List<EDBObject> convertModelsToEDBObjects(List<OpenEngSBModel> models, ConnectorId id) {
        List<EDBObject> result = new ArrayList<EDBObject>();
        if (models != null) {
            for (OpenEngSBModel model : models) {
                result.addAll(convertModelToEDBObject(model, id));
            }
        }
        return result;
    }

    /**
     * Converts an OpenEngSBModel object to an EDBObject (the version retrieving is not considered here. This is done in
     * the EDB directly).
     */
    public List<EDBObject> convertModelToEDBObject(OpenEngSBModel model, ConnectorId id) {
        List<EDBObject> objects = new ArrayList<EDBObject>();
        if (model != null) {
            convertSubModel(model, objects, id);
        }
        return objects;
    }

    /**
     * Recursive function to generate a list of EDBObjects out of a model object.
     */
    private String convertSubModel(OpenEngSBModel model, List<EDBObject> objects, ConnectorId id) {
        String oid = EDBConverterUtils.createOID(model, id.getDomainType(), id.getConnectorType());
        EDBObject object = new EDBObject(oid);

        for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (entry.getType().equals(FileWrapper.class)) {
                FileWrapper wrapper = (FileWrapper) entry.getValue();
                String content = Base64.encodeBase64String(wrapper.getContent());
                object.put(entry.getKey(), content);
                object.put(entry.getKey() + ".filename", wrapper.getFilename());
            } else if (entry.getType().equals(OpenEngSBModelWrapper.class)) {
                OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) entry.getValue();
                OpenEngSBModel temp = (OpenEngSBModel) ModelUtils.generateModelOutOfWrapper(wrapper,
                    model.getClass().getClassLoader());
                String subOid = convertSubModel(temp, objects, id);
                object.put(entry.getKey(), subOid);
            } else if (List.class.isAssignableFrom(entry.getType())) {
                List<?> list = (List<?>) entry.getValue();
                if (list == null || list.size() == 0) {
                    continue;
                }
                if (list.get(0).getClass().equals(OpenEngSBModelWrapper.class)) {
                    @SuppressWarnings("unchecked")
                    List<OpenEngSBModelWrapper> subList = (List<OpenEngSBModelWrapper>) entry.getValue();
                    if (subList == null) {
                        continue;
                    }
                    for (int i = 0; i < subList.size(); i++) {
                        OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) subList.get(i);
                        OpenEngSBModel temp =
                            (OpenEngSBModel) ModelUtils.generateModelOutOfWrapper(wrapper,
                                model.getClass().getClassLoader());
                        String subOid = convertSubModel(temp, objects, id);
                        object.put(entry.getKey() + i, subOid);
                    }
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        object.put(entry.getKey() + i, list.get(i));
                    }
                }

            } else {
                object.put(entry.getKey(), entry.getValue());
            }
        }
        object.put("domainId", id.getDomainType());
        object.put("connectorId", id.getConnectorType());
        object.put("instanceId", id.getInstanceId());

        objects.add(object);
        return oid;
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
}
