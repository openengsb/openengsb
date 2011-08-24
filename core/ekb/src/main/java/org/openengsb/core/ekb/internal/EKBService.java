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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.EngineeringKnowledgeBaseService;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service which implements the EngineeringKnowlegeBaseService. Also represents a proxy for simulating simple
 * OpenEngSBModel interfaces.
 */
public class EKBService implements EngineeringKnowledgeBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EKBService.class);

    private EngineeringDatabaseService edbService;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OpenEngSBModel> T createEmptyModelObject(Class<T> model, OpenEngSBModelEntry... entries) {
        LOGGER.debug("createEmpytModelObject for model interface {} called", model.getName());
        return (T) createModelObject(model, entries);
    }

    private Object createModelObject(Class<?> model, OpenEngSBModelEntry... entries) {
        ClassLoader classLoader = model.getClassLoader();
        Class<?>[] classes = new Class<?>[]{ OpenEngSBModel.class, model };
        InvocationHandler handler = makeHandler(model, entries);

        return Proxy.newProxyInstance(classLoader, classes, handler);
    }

    private EKBProxyHandler makeHandler(Class<?> model, OpenEngSBModelEntry[] entries) {
        EKBProxyHandler handler = new EKBProxyHandler(model, entries);
        return handler;
    }

    private Object createNewInstance(Class<?> model) {
        if (model.isInterface()) {
            return createModelObject(model);
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

    @SuppressWarnings("unchecked")
    private <T extends OpenEngSBModel> T convertEDBObjectToModel(Class<T> model, EDBObject object) {
        return (T) convertEDBObjectToUncheckedModel(model, object);
    }

    private Object convertEDBObjectToUncheckedModel(Class<?> model, EDBObject object) {
        Object instance = createNewInstance(model);
        boolean nothingSet = true;

        for (PropertyDescriptor propertyDescriptor : EKBUtils.getPropertyDescriptorsForClass(model)) {
            if (propertyDescriptor.getWriteMethod() == null) {
                continue;
            }
            Method setterMethod = propertyDescriptor.getWriteMethod();
            Object value = getValueForProperty(propertyDescriptor, object);
            if (value != null) {
                EKBUtils.invokeSetterMethod(setterMethod, instance, value);
                nothingSet = false;
            }
        }

        if (nothingSet) {
            return null;
        } else {
            return instance;
        }
    }

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
        } else if (object.containsKey(propertyName)) {
            if (parameterType.isEnum()) {
                value = getEnumValue(parameterType, value);
            }
        }

        return value;
    }

    private Class<?> getGenericParameterClass(Method setterMethod) {
        Type t = setterMethod.getGenericParameterTypes()[0];
        ParameterizedType pType = (ParameterizedType) t;
        Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
        return clazz;
    }

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

    private <T extends OpenEngSBModel> List<T> convertEDBObjectsToModelObjects(Class<T> model,
            List<EDBObject> objects) {
        List<T> models = new ArrayList<T>();
        for (EDBObject object : objects) {
            models.add(convertEDBObjectToModel(model, object));
        }
        return models;
    }

    @Override
    public <T extends OpenEngSBModel> T getModel(Class<T> model, String oid) {
        EDBObject object = edbService.getObject(oid);
        return convertEDBObjectToModel(model, object);
    }

    @Override
    public <T extends OpenEngSBModel> List<T> getModelHistory(Class<T> model, String oid) {
        return convertEDBObjectsToModelObjects(model, edbService.getHistory(oid));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> getModelHistoryForTimeRange(Class<T> model,
            String oid, Long from, Long to) {
        return convertEDBObjectsToModelObjects(model, edbService.getHistory(oid, from, to));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, String key, Object value) {
        return convertEDBObjectsToModelObjects(model, edbService.query(key, value));
    }

    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap) {
        return convertEDBObjectsToModelObjects(model, edbService.query(queryMap));
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
}
