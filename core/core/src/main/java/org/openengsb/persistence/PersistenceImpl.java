/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.persistence;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.openengsb.core.MessageProperties;
import org.openengsb.core.MethodCallHelper;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

public class PersistenceImpl implements Persistence {

    private MessageProperties msgProperties;

    private OpenEngSBEndpoint endpoint;

    private UniversalJaxbSerializer serializer = new UniversalJaxbSerializer();

    public PersistenceImpl(OpenEngSBEndpoint endpoint, MessageProperties msgProperties) {
        this.endpoint = endpoint;
        this.msgProperties = msgProperties;
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("create", PersistenceObject.class);
        MethodCallHelper.sendMethodCall(endpoint, persistenceEndpoint, method,
                new Object[] { toPersistenceObject(bean) }, msgProperties);
    }

    @Override
    public void create(List<Object> beans) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("create", List.class);
        MethodCallHelper.sendMethodCall(endpoint, persistenceEndpoint, method,
                new Object[] { toPersistenceObject(beans) }, msgProperties);
    }

    @Override
    public void delete(Object example) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("delete", Object.class);
        MethodCallHelper.sendMethodCall(endpoint, persistenceEndpoint, method,
                new Object[] { toPersistenceObject(example) }, msgProperties);
    }

    @Override
    public void delete(List<Object> examples) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("delete", List.class);
        MethodCallHelper.sendMethodCall(endpoint, persistenceEndpoint, method,
                new Object[] { toPersistenceObject(examples) }, msgProperties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> query(Object example) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("query", Object.class);
        List<PersistenceObject> result = (List<PersistenceObject>) MethodCallHelper.sendMethodCall(endpoint,
                persistenceEndpoint, method, new Object[] { toPersistenceObject(example) }, msgProperties);
        return toObject(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> query(List<Object> examples) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("query", List.class);
        List<PersistenceObject> result = (List<PersistenceObject>) MethodCallHelper.sendMethodCall(endpoint,
                persistenceEndpoint, method, new Object[] { toPersistenceObject(examples) }, msgProperties);
        return toObject(result);
    }

    @Override
    public void update(Object oldBean, Object newBean) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("update", Object.class, Object.class);
        MethodCallHelper.sendMethodCall(endpoint, persistenceEndpoint, method, new Object[] {
                toPersistenceObject(oldBean), toPersistenceObject(newBean) }, msgProperties);
    }

    @Override
    public void update(Map<Object, Object> beans) throws PersistenceException {
        QName persistenceEndpoint = getPersistenceEndpoint();
        Method method = getMethod("update", Map.class);
        MethodCallHelper.sendMethodCall(endpoint, persistenceEndpoint, method,
                new Object[] { toPersistenceObject(beans) }, msgProperties);
    }

    private Map<PersistenceObject, PersistenceObject> toPersistenceObject(Map<Object, Object> beans)
            throws PersistenceException {
        Map<PersistenceObject, PersistenceObject> result = new HashMap<PersistenceObject, PersistenceObject>();
        for (Entry<Object, Object> entry : beans.entrySet()) {
            result.put(toPersistenceObject(entry.getKey()), toPersistenceObject(entry.getValue()));
        }
        return result;
    }

    private List<PersistenceObject> toPersistenceObject(List<Object> beans) throws PersistenceException {
        List<PersistenceObject> result = new ArrayList<PersistenceObject>();
        for (Object bean : beans) {
            result.add(toPersistenceObject(bean));
        }
        return result;
    }

    private List<Object> toObject(List<PersistenceObject> persistenceObjects) throws PersistenceException {
        List<Object> result = new ArrayList<Object>();
        for (PersistenceObject persistenceObject : persistenceObjects) {
            result.add(toObject(persistenceObject));
        }
        return result;
    }

    private Object toObject(PersistenceObject persistenceObject) throws PersistenceException {
        try {
            Class<?> resultClass = getClass(persistenceObject.getClassName());
            return serializer.deserialize(resultClass, persistenceObject.getXml());
        } catch (JAXBException e) {
            throw new PersistenceException(e);
        }
    }

    private PersistenceObject toPersistenceObject(Object bean) throws PersistenceException {
        try {
            String xml = serializer.serialize(bean);
            return new PersistenceObject(xml, bean.getClass().getName());
        } catch (JAXBException e) {
            throw new PersistenceException(e);
        }
    }

    private QName getPersistenceEndpoint() {
        return new QName("urn:openengsb:persistence", "persistenceService");
    }

    private Method getMethod(String name, Class<?>... params) {
        try {
            return PersistenceInternal.class.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getClass(String type) {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
