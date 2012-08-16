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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.openengsb.core.api.edb.EDBConstants;
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
     * Tests if an EDBObject has the correct model class in which it should be converted. Returns false if the model
     * type is not fitting, returns true if the model type is fitting or model type is unknown.
     */
    private boolean checkEDBObjectModelType(EDBObject object, Class<?> model) {
        String modelClass = object.getString(EDBConstants.MODEL_TYPE);
        if (modelClass == null) {
            LOGGER.warn(String.format("The EDBObject with the oid %s has no model type information."
                    + "The resulting model may be a different model type than expected.", object.getOID()));
        }
        if (modelClass != null && !modelClass.equals(model.toString())) {
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
        Object instance = createNewInstance(model);

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
            Object obj = object.get(property);
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
            Object key = object.get(keyProperty);
            Object value = object.get(valueProperty);
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
            } else if (entry.getType().equals(FileWrapper.class)) {
                FileWrapper wrapper = (FileWrapper) entry.getValue();
                String content = Base64.encodeBase64String(wrapper.getContent());
                object.put(entry.getKey(), content);
                object.put(entry.getKey() + ".filename", wrapper.getFilename());
                object.put(entry.getKey() + ".type", FileWrapper.class.getName());
            } else if (entry.getType().equals(OpenEngSBModelWrapper.class)) {
                OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) entry.getValue();
                Object subModel = ModelUtils.generateModelOutOfWrapper(wrapper,
                    model.getClass().getClassLoader());
                OpenEngSBModel temp = (OpenEngSBModel) subModel;
                String subOid = convertSubModel(temp, objects, id);
                object.put(entry.getKey(), subOid);
                object.put(entry.getKey() + ".type", subModel.getClass().getName());
            } else if (List.class.isAssignableFrom(entry.getType())) {
                List<?> list = (List<?>) entry.getValue();
                if (list == null || list.size() == 0) {
                    continue;
                }
                Boolean modelItems = null;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (modelItems == null) {
                        modelItems = item.getClass().equals(OpenEngSBModelWrapper.class);
                    }
                    if (modelItems) {
                        item = createSubModelOutOfWrapper(item, model, objects, id);
                    }
                    object.put(entry.getKey() + i, item);
                    object.put(entry.getKey() + ".type", item.getClass().getName());
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
                        keyIsModel = ent.getKey().getClass().equals(OpenEngSBModelWrapper.class);
                    }
                    if (valueIsModel == null) {
                        valueIsModel = ent.getValue().getClass().equals(OpenEngSBModelWrapper.class);
                    }
                    Object key = ent.getKey();
                    Object value = ent.getValue();
                    if (keyIsModel) {
                        key = createSubModelOutOfWrapper(key, model, objects, id);
                    }
                    if (valueIsModel) {
                        value = createSubModelOutOfWrapper(key, model, objects, id);
                    }
                    object.put(entry.getKey() + i + ".key", key);
                    object.put(entry.getKey() + i + ".key.type", key.getClass().getName());
                    object.put(entry.getKey() + i + ".value", value);
                    object.put(entry.getKey() + i + ".value.type", value.getClass().getName());
                    i++;
                }
            } else {
                object.put(entry.getKey(), entry.getValue());
                object.put(entry.getKey() + ".value", entry.getValue());
            }
        }
        Class<?> modelType = ModelUtils.getModelClassOfOpenEngSBModelObject(model.getClass());
        object.put(EDBConstants.MODEL_TYPE, modelType.toString());
        object.put("domainId", id.getDomainType());
        object.put("connectorId", id.getConnectorType());
        object.put("instanceId", id.getInstanceId());
        objects.add(object);
        return oid;
    }

    /**
     * Create a sub model out of a wrapper object and returns the oid of the sub model.
     */
    private String createSubModelOutOfWrapper(Object subModel, OpenEngSBModel parent, List<EDBObject> objects,
            ConnectorId id) {
        OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) subModel;
        OpenEngSBModel temp =
            (OpenEngSBModel) ModelUtils.generateModelOutOfWrapper(wrapper,
                parent.getClass().getClassLoader());
        return convertSubModel(temp, objects, id);
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
}
