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

package org.openengsb.core.ekb.impl.internal.converter;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.math.NumberUtils;
import org.openengsb.core.api.model.FileWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
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
    public <T> T convertEDBObjectToModel(Class<T> model, EDBObject object) {
        return (T) convertEDBObjectToUncheckedModel(model, object);
    }

    /**
     * Converts a list of EDBObjects to a list of models of the given model type.
     */
    public <T> List<T> convertEDBObjectsToModelObjects(Class<T> model,
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
     * Tests if an EDBObject has the correct model class in which it should be converted. Returns false if the model
     * type is not fitting, returns true if the model type is fitting or model type is unknown.
     */
    private boolean checkEDBObjectModelType(EDBObject object, Class<?> model) {
        String modelClass = object.getString(EDBConstants.MODEL_TYPE);
        if (modelClass == null) {
            LOGGER.warn(String.format("The EDBObject with the oid %s has no model type information."
                    + "The resulting model may be a different model type than expected.", object.getOID()));
        }
        if (modelClass != null && !modelClass.equals(model.getName())) {
            return false;
        }
        return true;
    }

    /**
     * Converts an EDBObject to a model by analyzing the object and trying to call the corresponding setters of the
     * model.
     */
    private Object convertEDBObjectToUncheckedModel(Class<?> model, EDBObject object) {
        if (!checkEDBObjectModelType(object, model)) {
            return null;
        }
        Object instance = null;
        try {
            instance = model.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("InstantiationException while creating instance of model "
                    + model.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("IllegalAccessException while creating instance of model "
                    + model.getName(), e);
        }

        for (PropertyDescriptor propertyDescriptor : ModelUtils.getPropertyDescriptorsForClass(model)) {
            if (propertyDescriptor.getWriteMethod() == null) {
                continue;
            }
            Method setterMethod = propertyDescriptor.getWriteMethod();
            Object value = getValueForProperty(propertyDescriptor, object);
            if (value != null) {
                invokeSetterMethod(setterMethod, instance, value);
            }
        }
        return instance;
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
        Object value = object.getObject(propertyName);
        Class<?> parameterType = setterMethod.getParameterTypes()[0];

        // TODO: OPENENGSB-2719 do that in a better way than just an if-else series
        if (object.containsKey(propertyName + "0.key")) {
            List<Class<?>> classes = getGenericMapParameterClasses(setterMethod);
            value = getMapValue(classes.get(0), classes.get(1), propertyName, object);
        } else if (object.containsKey(propertyName + "0")) {
            Class<?> clazz = getGenericListParameterClass(setterMethod);
            value = getListValue(clazz, propertyName, object);
        } else if (value == null) {
            return null;
        } else if (OpenEngSBModel.class.isAssignableFrom(parameterType)) {
            value = convertEDBObjectToUncheckedModel(parameterType, edbService.getObject((String) value));
        } else if (parameterType.equals(FileWrapper.class)) {
            FileWrapper wrapper = new FileWrapper();
            String filename = object.getString(propertyName + ".filename");
            String content = (String) value;
            wrapper.setFilename(filename);
            wrapper.setContent(Base64.decodeBase64(content));
            value = wrapper;
        } else if (parameterType.equals(File.class)) {
            return null;
        } else if (object.containsKey(propertyName)) {
            if (parameterType.isEnum()) {
                value = getEnumValue(parameterType, value);
            } else if (Number.class.isAssignableFrom(parameterType)) {
                value = NumberUtils.createNumber((String) value);
            }
        }
        return value;
    }

    /**
     * Get the type of the list parameter of a setter.
     */
    private Class<?> getGenericListParameterClass(Method setterMethod) {
        return getGenericParameterClasses(setterMethod, 1).get(0);
    }

    /**
     * Get the type of the map parameter of a setter
     */
    private List<Class<?>> getGenericMapParameterClasses(Method setterMethod) {
        return getGenericParameterClasses(setterMethod, 2);
    }

    /**
     * Loads the generic parameter classes up to the given depth (1 for lists, 2 for maps)
     */
    private List<Class<?>> getGenericParameterClasses(Method setterMethod, int depth) {
        Type t = setterMethod.getGenericParameterTypes()[0];
        ParameterizedType pType = (ParameterizedType) t;
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (int i = 0; i < depth; i++) {
            classes.add((Class<?>) pType.getActualTypeArguments()[i]);
        }
        return classes;
    }

    /**
     * Gets a list object out of an EDBObject.
     */
    private Object getListValue(Class<?> type, String propertyName, EDBObject object) {
        List<Object> temp = new ArrayList<Object>();
        for (int i = 0;; i++) {
            String property = propertyName + i;
            Object obj = object.getObject(property);
            if (obj == null) {
                break;
            }
            if (OpenEngSBModel.class.isAssignableFrom(type)) {
                obj = convertEDBObjectToUncheckedModel(type, edbService.getObject(object.getString(property)));
            }
            temp.add(obj);
        }
        return temp;
    }

    /**
     * Gets a map object out of an EDBObject.
     */
    private Object getMapValue(Class<?> keyType, Class<?> valueType, String propertyName, EDBObject object) {
        Map<Object, Object> temp = new HashMap<Object, Object>();
        for (int i = 0;; i++) {
            String keyProperty = propertyName + i + ".key";
            String valueProperty = propertyName + i + ".value";
            if (!object.containsKey(keyProperty)) {
                break;
            }
            Object key = object.getObject(keyProperty);
            Object value = object.getObject(valueProperty);
            if (OpenEngSBModel.class.isAssignableFrom(keyType)) {
                key = convertEDBObjectToUncheckedModel(keyType, edbService.getObject(key.toString()));
            }
            if (OpenEngSBModel.class.isAssignableFrom(valueType)) {
                value = convertEDBObjectToUncheckedModel(valueType, edbService.getObject(value.toString()));
            }
            temp.put(key, value);
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
     * Converts the models of an EKBCommit to EDBObjects and return an object which contains the three corresponding
     * lists
     */
    public ConvertedCommit convertEKBCommit(EKBCommit commit) {
        ConvertedCommit result = new ConvertedCommit();
        ConnectorInformation information = EDBConverterUtils.getConnectorInformationOfEKBCommit(commit);
        result.setInserts(convertModelsToEDBObjects(commit.getInserts(), information));
        result.setUpdates(convertModelsToEDBObjects(commit.getUpdates(), information));
        result.setDeletes(convertModelsToEDBObjects(commit.getDeletes(), information));
        return result;
    }

    /**
     * Convert a list of models to a list of EDBObjects (the version retrieving is not considered here. This is done in
     * the EDB directly).
     */
    public List<EDBObject> convertModelsToEDBObjects(List<OpenEngSBModel> models, ConnectorInformation info) {
        List<EDBObject> result = new ArrayList<EDBObject>();
        if (models != null) {
            for (Object model : models) {
                result.addAll(convertModelToEDBObject(model, info));
            }
        }
        return result;
    }

    /**
     * Converts an OpenEngSBModel object to an EDBObject (the version retrieving is not considered here. This is done in
     * the EDB directly).
     */
    public List<EDBObject> convertModelToEDBObject(Object model, ConnectorInformation info) {
        if (!OpenEngSBModel.class.isAssignableFrom(model.getClass())) {
            throw new IllegalArgumentException("This function need to get a model passed");
        }
        List<EDBObject> objects = new ArrayList<EDBObject>();
        if (model != null) {
            convertSubModel((OpenEngSBModel) model, objects, info);
        }
        return objects;
    }

    /**
     * Recursive function to generate a list of EDBObjects out of a model object.
     */
    private String convertSubModel(OpenEngSBModel model, List<EDBObject> objects, ConnectorInformation info) {
        String oid = EDBConverterUtils.createOID(model, info.getDomainId(), info.getConnectorId());
        EDBObject object = new EDBObject(oid);
        for (OpenEngSBModelEntry entry : model.toOpenEngSBModelEntries()) {
            if (entry.getValue() == null) {
                continue;
            } else if (entry.getType().equals(FileWrapper.class)) {
                try {
                    FileWrapper wrapper = (FileWrapper) entry.getValue();
                    String content = Base64.encodeBase64String(wrapper.getContent());
                    object.putEDBObjectEntry(entry.getKey(), content, String.class);
                    object.putEDBObjectEntry(entry.getKey() + ".filename", wrapper.getFilename(), String.class);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            } else if (OpenEngSBModel.class.isAssignableFrom(entry.getType())) {
                OpenEngSBModel temp = (OpenEngSBModel) entry.getValue();
                String subOid = convertSubModel(temp, objects, info);
                object.putEDBObjectEntry(entry.getKey(), subOid, String.class);
            } else if (List.class.isAssignableFrom(entry.getType())) {
                List<?> list = (List<?>) entry.getValue();
                if (list == null || list.size() == 0) {
                    continue;
                }
                Boolean modelItems = null;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (modelItems == null) {
                        modelItems = OpenEngSBModel.class.isAssignableFrom(item.getClass());
                    }
                    if (modelItems) {
                        item = convertSubModel((OpenEngSBModel) item, objects, info);
                    }
                    object.putEDBObjectEntry(entry.getKey() + i, item, item.getClass());
                }
            } else if (Map.class.isAssignableFrom(entry.getType())) {
                Map<?, ?> map = (Map<?, ?>) entry.getValue();
                if (map == null || map.size() == 0) {
                    continue;
                }
                Boolean keyIsModel = null;
                Boolean valueIsModel = null;
                int i = 0;
                for (Map.Entry<?, ?> ent : map.entrySet()) {
                    if (keyIsModel == null) {
                        keyIsModel = OpenEngSBModel.class.isAssignableFrom(ent.getKey().getClass());
                    }
                    if (valueIsModel == null) {
                        valueIsModel = OpenEngSBModel.class.isAssignableFrom(ent.getValue().getClass());
                    }
                    Object key = ent.getKey();
                    Object value = ent.getValue();
                    if (keyIsModel) {
                        key = convertSubModel((OpenEngSBModel) key, objects, info);
                    }
                    if (valueIsModel) {
                        value = convertSubModel((OpenEngSBModel) value, objects, info);
                    }
                    object.putEDBObjectEntry(entry.getKey() + i + ".key", key, key.getClass());
                    object.putEDBObjectEntry(entry.getKey() + i + ".value", value, value.getClass());
                    i++;
                }
            } else {
                object.putEDBObjectEntry(entry.getKey(), entry.getValue(), entry.getClass());
            }
        }
        Class<?> modelType = model.getClass();
        object.putEDBObjectEntry(EDBConstants.MODEL_TYPE, modelType.getName(), String.class);
        object.putEDBObjectEntry("domainId", info.getDomainId(), String.class);
        object.putEDBObjectEntry("connectorId", info.getConnectorId(), String.class);
        object.putEDBObjectEntry("instanceId", info.getInstanceId(), String.class);
        objects.add(object);
        return oid;
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
}
