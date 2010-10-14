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

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.nq.NativeQuery;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

public class NeodatisPersistenceService implements PersistenceService {

    private String dbFile;

    @Override
    public void create(Object bean) throws PersistenceException {
        ODB database = ODBFactory.open(dbFile);
        database.store(bean);
        database.close();
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        if (beans.isEmpty()) {
            return;
        }
        ODB database = ODBFactory.open(dbFile);
        for (Object bean : beans) {
            database.store(bean);
        }
        database.close();
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        ODB database = ODBFactory.open(dbFile);
        List<TYPE> toDelete = queryByExample(database, example);
        if (toDelete.isEmpty()) {
            database.close();
            throw new PersistenceException(String.format(
                "Element '%s' cannot be deleted because it was not found in database.", example));
        }
        for (TYPE element : toDelete) {
            database.delete(element);
        }
        database.close();
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        if (examples.isEmpty()) {
            return;
        }
        ODB database = ODBFactory.open(dbFile);
        List<TYPE> toDelete = new ArrayList<TYPE>();
        for (TYPE example : examples) {
            toDelete.addAll(queryByExample(database, example));
        }
        if (toDelete.isEmpty()) {
            database.close();
            throw new PersistenceException(
                "None of the entered elements cannot be deleted because it was not found in database.");
        }
        for (TYPE element : toDelete) {
            database.delete(element);
        }
        database.close();
    }

    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        ODB database = ODBFactory.open(dbFile);
        List<TYPE> result = queryByExample(database, example);
        database.close();
        return result;
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        if (examples.isEmpty()) {
            return new ArrayList<TYPE>();
        }
        ODB database = ODBFactory.open(dbFile);
        List<TYPE> result = new ArrayList<TYPE>();
        for (TYPE example : examples) {
            result.addAll(queryByExample(database, example));
        }
        database.close();
        return result;
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        ODB database = ODBFactory.open(dbFile);
        doUpdate(database, oldBean, newBean);
        database.close();
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
        ODB database = ODBFactory.open(dbFile);
        try {
            for (Entry<TYPE, TYPE> entry : beans.entrySet()) {
                doUpdate(database, entry.getKey(), entry.getValue());
            }
        } catch (PersistenceException e) {
            database.rollback();
            throw e;
        } finally {
            database.close();
        }
    }

    @SuppressWarnings({ "unchecked", "serial" })
    private <TYPE> List<TYPE> queryByExample(ODB database, final TYPE example) {
        final Field[] fields = example.getClass().getDeclaredFields();
        IQuery query = new NativeQuery() {

            @Override
            public boolean match(Object object) {
                TYPE compare = (TYPE) object;
                boolean equal = true;
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        Object exampleField = field.get(example);
                        Object compareField = field.get(compare);
                        if (exampleField != null && !exampleField.equals(compareField)) {
                            equal = false;
                            break;
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return equal;
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

    public void setDbFile(String dbFile) {
        this.dbFile = dbFile;
    }

}
