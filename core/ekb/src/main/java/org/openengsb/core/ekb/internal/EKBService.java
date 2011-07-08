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
import java.lang.reflect.Proxy;
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

        ClassLoader classLoader = model.getClassLoader();
        Class<?>[] classes = new Class<?>[]{ OpenEngSBModel.class, model };
        InvocationHandler handler = makeHandler(model.getMethods(), entries);

        return (T) Proxy.newProxyInstance(classLoader, classes, handler);
    }

    private EKBProxyHandler makeHandler(Method[] methods, OpenEngSBModelEntry[] entries) {
        EKBProxyHandler handler = new EKBProxyHandler(methods, entries);
        return handler;
    }

    private <T extends OpenEngSBModel> T createInstance(Class<T> model) {
        if (model.isInterface()) {
            return createEmptyModelObject(model);
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

    private <T extends OpenEngSBModel> T convertEDBObjectToModel(Class<T> model, EDBObject object) {
        T instance = createInstance(model);
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(model);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method setterMethod = propertyDescriptor.getWriteMethod();
                String propertyName = propertyDescriptor.getName();
                if (object.containsKey(propertyDescriptor.getName())) {
                    try {
                        setterMethod.invoke(instance, object.get(propertyName));
                    } catch (IllegalArgumentException ex) {
                        LOGGER.error("illegal argument exception when invoking {} with argument {}",
                            setterMethod.getName(), object.get(propertyName));
                    } catch (IllegalAccessException ex) {
                        LOGGER.error("illegal access exception when invoking {} with argument {}",
                            setterMethod.getName(), object.get(propertyName));
                    } catch (InvocationTargetException ex) {
                        LOGGER.error("invocatin target exception when invoking {} with argument {}",
                            setterMethod.getName(), object.get(propertyName));
                    }
                }
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("instantiation exception while trying to create instance of class {}", model.getName());
        }

        return instance;
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
