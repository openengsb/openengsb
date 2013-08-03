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

package org.openengsb.core.ekb.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.FileWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The EDBConverter class responsibility is the converting between EDBObjects and models and the vice-versa.
 */
public class EDBConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EDBConverter.class);
    private EngineeringDatabaseService edbService;
    
    public EDBConverter(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }

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
        EDBConverterUtils.filterEngineeringObjectInformation(object, model);
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        for (PropertyDescriptor propertyDescriptor : getPropertyDescriptorsForClass(model)) {
            if (propertyDescriptor.getWriteMethod() == null
                    || propertyDescriptor.getName().equals(ModelUtils.MODEL_TAIL_FIELD_NAME)) {
                continue;
            }
            Object value = getValueForProperty(propertyDescriptor, object);
            Class<?> propertyClass = propertyDescriptor.getPropertyType();
            if (propertyClass.isPrimitive()) {
                entries.add(new OpenEngSBModelEntry(propertyDescriptor.getName(), value, ClassUtils
                    .primitiveToWrapper(propertyClass)));
            } else {
                entries.add(new OpenEngSBModelEntry(propertyDescriptor.getName(), value, propertyClass));
            }
        }

        for (Map.Entry<String, EDBObjectEntry> objectEntry : object.entrySet()) {
            EDBObjectEntry entry = objectEntry.getValue();
            Class<?> entryType;
            try {
                entryType = model.getClassLoader().loadClass(entry.getType());
                entries.add(new OpenEngSBModelEntry(entry.getKey(), entry.getValue(), entryType));
            } catch (ClassNotFoundException e) {
                LOGGER.error("Unable to load class {} of the model tail", entry.getType());
            }
        }
        return ModelUtils.createModel(model, entries);
    }
    
    /**
     * Returns all property descriptors for a given class.
     */
    private List<PropertyDescriptor> getPropertyDescriptorsForClass(Class<?> clasz) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clasz);
            return Arrays.asList(beanInfo.getPropertyDescriptors());
        } catch (IntrospectionException e) {
            LOGGER.error("instantiation exception while trying to create instance of class {}", clasz.getName());
        }
        return Lists.newArrayList();
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
        if (Map.class.isAssignableFrom(parameterType)) {
            List<Class<?>> classes = getGenericMapParameterClasses(setterMethod);
            value = getMapValue(classes.get(0), classes.get(1), propertyName, object);
        } else if (List.class.isAssignableFrom(parameterType)) {
            Class<?> clazz = getGenericListParameterClass(setterMethod);
            value = getListValue(clazz, propertyName, object);
        } else if (parameterType.isArray()) {
            Class<?> clazz = parameterType.getComponentType();
            value = getArrayValue(clazz, propertyName, object);
        } else if (value == null) {
            return null;
        } else if (OpenEngSBModel.class.isAssignableFrom(parameterType)) {
            Object timestamp = object.getObject(EDBConstants.MODEL_TIMESTAMP);
            Long time = System.currentTimeMillis();
            if (timestamp != null) {
                try {
                    time = Long.parseLong(timestamp.toString());
                } catch (NumberFormatException e) {
                    LOGGER.warn("The model with the oid {} has an invalid timestamp.", object.getOID());
                }
            }
            EDBObject obj = edbService.getObject((String) value, time);
            value = convertEDBObjectToUncheckedModel(parameterType, obj);
            object.remove(propertyName);
        } else if (parameterType.equals(FileWrapper.class)) {
            FileWrapper wrapper = new FileWrapper();
            String filename = object.getString(propertyName + EDBConverterUtils.FILEWRAPPER_FILENAME_SUFFIX);
            String content = (String) value;
            wrapper.setFilename(filename);
            wrapper.setContent(Base64.decodeBase64(content));
            value = wrapper;
            object.remove(propertyName + EDBConverterUtils.FILEWRAPPER_FILENAME_SUFFIX);
        } else if (parameterType.equals(File.class)) {
            return null;
        } else if (object.containsKey(propertyName)) {
            if (parameterType.isEnum()) {
                value = getEnumValue(parameterType, value);
            }
        }
        object.remove(propertyName);
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
    @SuppressWarnings("unchecked")
    private <T> List<T> getListValue(Class<T> type, String propertyName, EDBObject object) {
        List<T> temp = new ArrayList<T>();
        for (int i = 0;; i++) {
            String property = EDBConverterUtils.getEntryNameForList(propertyName, i);
            Object obj = object.getObject(property);
            if (obj == null) {
                break;
            }
            if (OpenEngSBModel.class.isAssignableFrom(type)) {
                obj = convertEDBObjectToUncheckedModel(type, edbService.getObject(object.getString(property)));
            }
            temp.add((T) obj);
            object.remove(property);
        }
        return temp;
    }
    
    /**
     * Gets an array object out of an EDBObject.
     */
    @SuppressWarnings("unchecked")
    private <T> T[] getArrayValue(Class<T> type, String propertyName, EDBObject object) {
        List<T> elements = getListValue(type, propertyName, object); 
        T[] ar = (T[]) Array.newInstance(type, elements.size());
        return elements.toArray(ar);
    }

    /**
     * Gets a map object out of an EDBObject.
     */
    private Object getMapValue(Class<?> keyType, Class<?> valueType, String propertyName, EDBObject object) {
        Map<Object, Object> temp = new HashMap<Object, Object>();
        for (int i = 0;; i++) {
            String keyProperty = EDBConverterUtils.getEntryNameForMapKey(propertyName, i);
            String valueProperty = EDBConverterUtils.getEntryNameForMapValue(propertyName, i);
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
            object.remove(keyProperty);
            object.remove(valueProperty);
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
        String contextId = ContextHolder.get().getCurrentContextId();
        String oid = EDBConverterUtils.createOID(model, contextId);
        EDBObject object = new EDBObject(oid);
        try {
            EDBConverterUtils.fillEDBObjectWithEngineeringObjectInformation(object, model);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Unable to fill completely the EngineeringObjectInformation into the EDBObject", e);
            throw new EKBException("Unable to fill completely the EngineeringObjectInformation into the EDBObject", e);
        }
        for (OpenEngSBModelEntry entry : model.toOpenEngSBModelEntries()) {
            if (entry.getValue() == null) {
                continue;
            } else if (entry.getType().equals(FileWrapper.class)) {
                try {
                    FileWrapper wrapper = (FileWrapper) entry.getValue();
                    String content = Base64.encodeBase64String(wrapper.getContent());
                    object.putEDBObjectEntry(entry.getKey(), content, String.class);
                    object.putEDBObjectEntry(entry.getKey() + EDBConverterUtils.FILEWRAPPER_FILENAME_SUFFIX,
                        wrapper.getFilename(), String.class);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
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
                    String entryName = EDBConverterUtils.getEntryNameForList(entry.getKey(), i);
                    object.putEDBObjectEntry(entryName, item, item.getClass());
                }
            } else if (entry.getType().isArray()) {
                Object[] array = (Object[]) entry.getValue();
                if (array == null || array.length == 0) {
                    continue;
                }
                Boolean modelItems = null;
                for (int i = 0; i < array.length; i++) {
                    Object item = array[i];
                    if (modelItems == null) {
                        modelItems = OpenEngSBModel.class.isAssignableFrom(item.getClass());
                    }
                    if (modelItems) {
                        item = convertSubModel((OpenEngSBModel) item, objects, info);
                    }
                    String entryName = EDBConverterUtils.getEntryNameForList(entry.getKey(), i);
                    object.putEDBObjectEntry(entryName, item, item.getClass());
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
                    object.putEDBObjectEntry(EDBConverterUtils.getEntryNameForMapKey(entry.getKey(), i), key);
                    object.putEDBObjectEntry(EDBConverterUtils.getEntryNameForMapValue(entry.getKey(), i), value);
                    i++;
                }
            } else {
                object.putEDBObjectEntry(entry.getKey(), entry.getValue(), entry.getType());
            }
        }
        object.putEDBObjectEntry(EDBConstants.MODEL_TYPE, model.retrieveModelName());
        object.putEDBObjectEntry(EDBConstants.MODEL_TYPE_VERSION, model.retrieveModelVersion());
        object.putEDBObjectEntry("domainId", info.getDomainId());
        object.putEDBObjectEntry("connectorId", info.getConnectorId());
        object.putEDBObjectEntry("instanceId", info.getInstanceId());
        object.putEDBObjectEntry("contextId", contextId);
        objects.add(object);
        return oid;
    }
}
