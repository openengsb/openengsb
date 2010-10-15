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

package org.openengsb.core.persistence.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.nq.NativeQuery;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

public class NeodatisPersistenceService implements PersistenceService {

    private String dbFile;

    private Semaphore semaphore = new Semaphore(1);

    private ClassLoader loader;

    public NeodatisPersistenceService(String dbFile, ClassLoader loader) {
        this.dbFile = dbFile;
        this.loader = loader;
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        ODB database = openDatabase(bean);
        try {
            database.store(bean);
        } finally {
            closeDatabase(database);
        }
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        if (beans.isEmpty()) {
            return;
        }
        ODB database = openDatabase(beans.get(0));
        try {
            for (Object bean : beans) {
                database.store(bean);
            }
        } finally {
            closeDatabase(database);
        }
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        ODB database = openDatabase(example);
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
            closeDatabase(database);
        }
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        if (examples.isEmpty()) {
            return;
        }
        ODB database = openDatabase(examples.get(0));
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
            closeDatabase(database);
        }
    }

    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        ODB database = openDatabase(example);
        try {
            List<TYPE> result = queryByExample(database, example);
            return result;
        } finally {
            closeDatabase(database);
        }
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        if (examples.isEmpty()) {
            return new ArrayList<TYPE>();
        }
        ODB database = openDatabase(examples.get(0));
        try {
            List<TYPE> result = new ArrayList<TYPE>();
            for (TYPE example : examples) {
                result.addAll(queryByExample(database, example));
            }
            return result;
        } finally {
            closeDatabase(database);
        }
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        ODB database = openDatabase(oldBean);
        try {
            doUpdate(database, oldBean, newBean);
        } finally {
            closeDatabase(database);
        }
    }

    private <TYPE> void doUpdate(ODB database, TYPE oldBean, TYPE newBean) throws PersistenceException {
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
        ODB database = openDatabase(beans.keySet().iterator().next());
        try {
            for (Entry<TYPE, TYPE> entry : beans.entrySet()) {
                doUpdate(database, entry.getKey(), entry.getValue());
            }
        } catch (PersistenceException e) {
            database.rollback();
            throw e;
        } finally {
            closeDatabase(database);
        }
    }

    @SuppressWarnings({"unchecked", "serial"})
    private <TYPE> List<TYPE> queryByExample(ODB database, final TYPE example) {
        final Field[] fields = example.getClass().getDeclaredFields();
        IQuery query = new NativeQuery() {

            @Override
            public boolean match(Object object) {
                TYPE compare = (TYPE) object;
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        Object exampleField = field.get(example);
                        Object compareField = field.get(compare);
                        if (exampleField != null && !exampleField.equals(compareField)) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                return true;
            }

            @Override
            public Class<?> getObjectType() {
                return example.getClass();
            }

        };
        Objects<Object> objects = database.getObjects(query);
        List<TYPE> retVal = new ArrayList<TYPE>();
        while (objects.hasNext()) {
            retVal.add((TYPE) objects.next());
        }
        return retVal;
    }

    private void closeDatabase(ODB database) {
        database.close();
        semaphore.release();
    }

    private ODB openDatabase(Object object) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            ODB database = null;
            synchronized (OdbConfiguration.class) {
                OdbConfiguration.setClassLoader(loader);
                database = ODBFactory.open(dbFile);
            }
            return database;
        } catch (RuntimeException re) {
            semaphore.release();
            throw re;
        }
    }

}
