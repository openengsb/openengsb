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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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
        InvocationHandler handler = makeHandler(model.getMethods(), entries);

        return Proxy.newProxyInstance(classLoader, classes, handler);
    }

    private EKBProxyHandler makeHandler(Method[] methods, OpenEngSBModelEntry[] entries) {
        EKBProxyHandler handler = new EKBProxyHandler(methods, entries);
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
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LOGGER.error("illegal access exception while trying to create instance of class {}", model.getName());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends OpenEngSBModel> T convertEDBObjectToModel(Class<T> model, EDBObject object) {
        return (T) convertEDBObjectToModel(model, object, "");
    }

    private Object convertEDBObjectToModel(Class<?> model, EDBObject object, String propertyPrefix) {
        Object instance = createNewInstance(model);
        boolean nothingSet = true;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(model);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method setterMethod = propertyDescriptor.getWriteMethod();
                String propertyName = propertyPrefix + propertyDescriptor.getName();
                Object value = object.get(propertyName);

                if (object.containsKey(propertyName) || object.containsKey(propertyName + "0")) {
                    Class<?> type = setterMethod.getParameterTypes()[0];
                    if (type.isEnum()) {
                        value = getEnumValue(type, value);
                    } else if (List.class.isAssignableFrom(type)) {
                        Class<?> clazz = getGenericParameterClass(setterMethod);
                        value = getListValue(clazz, propertyName, object);
                    }
                } else if (setterMethod != null
                        && setterMethod.getParameterTypes()[0].isInterface()) {
                    if (List.class.isAssignableFrom(setterMethod.getParameterTypes()[0])) {
                        Class<?> clazz = getGenericParameterClass(setterMethod);
                        value = getListValue(clazz, propertyName, object);
                    } else {
                        value = convertEDBObjectToModel(setterMethod.getParameterTypes()[0], object,
                                   propertyName + ".");
                    }
                }
                if (value != null) {
                    setValueInInstance(instance, value, setterMethod);
                    nothingSet = false;
                }
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("instantiation exception while trying to create instance of class {}", model.getName());
        }

        if (nothingSet) {
            return null;
        } else {
            return instance;
        }
    }

    private Class<?> getGenericParameterClass(Method setterMethod) {
        Type t = setterMethod.getGenericParameterTypes()[0];
        try {
            ParameterizedType pType = (ParameterizedType) t;
            Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getListValue(Class<?> type, String propertyName, EDBObject object) {
        List<Object> temp = new ArrayList<Object>();
        for (int i = 0;; i++) {
            Object obj;

            if (type.isInterface() && !List.class.isAssignableFrom(type)) {
                obj = convertEDBObjectToModel(type, object, propertyName + i + ".");
            } else {
                obj = object.get(propertyName + i);
            }

            if (obj == null) {
                break;
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

    private void setValueInInstance(Object instance, Object value, Method setterMethod) {
        try {
            setterMethod.invoke(instance, value);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("illegal argument exception when invoking {} with argument {}",
                setterMethod.getName(), value);
        } catch (IllegalAccessException ex) {
            LOGGER.error("illegal access exception when invoking {} with argument {}",
                setterMethod.getName(), value);
        } catch (InvocationTargetException ex) {
            LOGGER.error("invocatin target exception when invoking {} with argument {}",
                setterMethod.getName(), value);
        }
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
