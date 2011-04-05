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

package org.openengsb.core.persistence.internal;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.query.nq.NativeQuery;
import org.openengsb.core.api.persistence.IgnoreInQueries;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceService;
import org.osgi.framework.Bundle;

public class NeodatisPersistenceService implements PersistenceService {

    private final String dbFile;

    private final Semaphore semaphore = new Semaphore(1);

    private final Bundle bundle;

    private ODB database;

    private CustomClassLoader loader;

    public NeodatisPersistenceService(String dbFile, Bundle bundle) {
        this.dbFile = dbFile;
        this.bundle = bundle;
        loader = new CustomClassLoader(this.getClass().getClassLoader(), this.bundle);
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        openTransaction(bean);
        try {
            database.store(bean);
        } finally {
            closeTransaction();
        }
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        if (beans.isEmpty()) {
            return;
        }
        openTransaction(beans.get(0));
        try {
            for (Object bean : beans) {
                database.store(bean);
            }
        } finally {
            closeTransaction();
        }
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        openTransaction(example);
        try {
            List<TYPE> toDelete = queryByExample(database, example);
            if (toDelete.isEmpty()) {
                throw new PersistenceException(String.format(
                    "Element '%s' cannot be deleted because it was not found in database.", example));
            }
            for (TYPE element : toDelete) {
                database.delete(element);
            }
        } finally {
            closeTransaction();
        }
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        if (examples.isEmpty()) {
            return;
        }
        openTransaction(examples.get(0));
        try {
            List<TYPE> toDelete = new ArrayList<TYPE>();
            for (TYPE example : examples) {
                toDelete.addAll(queryByExample(database, example));
            }
            if (toDelete.isEmpty()) {
                throw new PersistenceException(
                    "None of the entered elements cannot be deleted because it was not found in database.");
            }
            for (TYPE element : toDelete) {
                database.delete(element);
            }
        } finally {
            closeTransaction();
        }
    }

    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        openTransaction(example);
        List<TYPE> result = new ArrayList<TYPE>();
        try {
            result.addAll(queryByExample(database, example));
            return result;
        } finally {
            closeTransaction();
        }
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        if (examples.isEmpty()) {
            return new ArrayList<TYPE>();
        }
        openTransaction(examples.get(0));
        List<TYPE> result = new ArrayList<TYPE>();
        try {
            for (TYPE example : examples) {
                result.addAll(queryByExample(database, example));
            }
            return result;
        } finally {
            closeTransaction();
        }
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        openTransaction(oldBean);
        try {
            doUpdate(oldBean, newBean);
        } finally {
            closeTransaction();
        }
    }

    private <TYPE> void doUpdate(TYPE oldBean, TYPE newBean) throws PersistenceException {
        List<TYPE> queryResult = queryByExample(database, oldBean);
        if (queryResult.isEmpty()) {
            throw new PersistenceException("Could not update element '" + oldBean
                    + "', because it was not found in the database.");
        } else if (queryResult.size() > 1) {
            throw new PersistenceException("Could not update element '" + oldBean
                    + "', because it is stored multiple times in the database.");
        }
        database.delete(queryResult.get(0));
        database.store(newBean);
    }

    @Override
    public <TYPE> void update(Map<TYPE, TYPE> beans) throws PersistenceException {
        if (beans.isEmpty()) {
            return;
        }
        openTransaction(beans.keySet().iterator().next());
        try {
            for (Entry<TYPE, TYPE> entry : beans.entrySet()) {
                doUpdate(entry.getKey(), entry.getValue());
            }
        } catch (PersistenceException e) {
            database.rollback();
            throw e;
        } finally {
            closeTransaction();
        }
    }

    private <TYPE> List<TYPE> queryByExample(ODB database, TYPE example) {
        try {
            List<Method> getters = reflectGettersFromPersistenceClass(example.getClass());

            NeodatisGetterQuery<TYPE> query = new NeodatisGetterQuery<TYPE>(getters, example);

            List<TYPE> result = queryNeodatis(database, query);
            return result;
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Method> reflectGettersFromPersistenceClass(Class<?> clazz) throws IntrospectionException {
        List<Method> methods = new ArrayList<Method>();

        BeanInfo info = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] properties = info.getPropertyDescriptors();
        for (PropertyDescriptor property : properties) {
            final Method getter = property.getReadMethod();
            if (getter != null && Modifier.isPublic(getter.getModifiers())
                    && getter.getAnnotation(IgnoreInQueries.class) == null) {
                methods.add(getter);
            }
        }

        return methods;
    }

    @SuppressWarnings("unchecked")
    private <TYPE> List<TYPE> queryNeodatis(ODB database, NativeQuery query) {
        Objects<Object> objects = database.getObjects(query);
        List<TYPE> retVal = new ArrayList<TYPE>();
        while (objects.hasNext()) {
            retVal.add((TYPE) objects.next());
        }
        return retVal;
    }

    private void openTransaction(Object prototype) {
        try {
            semaphore.acquire();
            if (prototype != null) {
                loader.addClassToPool(prototype.getClass());
            }
            openDatabase();
        } catch (InterruptedException e) {
            semaphore.release();
            throw new RuntimeException(e);
        }
    }

    private void closeTransaction() {
        database.close();
        semaphore.release();
    }

    private void openDatabase() {
        synchronized (OdbConfiguration.class) {
            OdbConfiguration.setClassLoader(loader);
            database = ODBFactory.open(dbFile);
        }
    }

}
