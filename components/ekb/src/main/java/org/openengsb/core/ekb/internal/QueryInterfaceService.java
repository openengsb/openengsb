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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.QueryInterface;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the QueryInterface service. It's main responsibilities are the loading of
 * elements from the EDB and converting them to the correct format.
 */
public class QueryInterfaceService implements QueryInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterfaceService.class);
    
    private EngineeringDatabaseService edbService;

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

    @SuppressWarnings("unchecked")
    private <T extends OpenEngSBModel> T convertEDBObjectToModel(Class<T> model, EDBObject object) {
        return (T) convertEDBObjectToUncheckedModel(model, object);
    }

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
    
    @Override
    public <T extends OpenEngSBModel> List<T> queryForModels(Class<T> model, Map<String, Object> queryMap,
            Long timestamp) {
        return convertEDBObjectsToModelObjects(model, edbService.query(queryMap, timestamp));
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
}
