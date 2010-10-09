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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
import com.db4o.reflect.jdk.JdkReflector;

public class DB4OPersistenceService implements PersistenceService {

    private String dbFile;

    private SmartJdkLoader jdkLoader;

    private ObjectContainer database;

    private Semaphore semaphore = new Semaphore(1);

    @Override
    public void create(Object bean) throws PersistenceException {
        openDataBase(bean);
        database.store(bean);
        commit(bean);
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        if (beans.isEmpty()) {
            return;
        }

        openDataBase(beans.get(0));
        for (Object bean : beans) {
            database.store(bean);
        }

        commit(beans);
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        openDataBase(example);

        List<TYPE> toDelete = database.queryByExample(example);
        if (toDelete.isEmpty()) {
            commit();
            throw new PersistenceException("Element '" + example
                    + "' cannot be deleted because it was not found in database.");
        }
        for (TYPE element : toDelete) {
            database.delete(element);
        }

        commit();
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        if (examples.isEmpty()) {
            return;
        }

        openDataBase(examples.get(0));

        List<TYPE> toDelete = new ArrayList<TYPE>();

        for (TYPE example : examples) {
            toDelete.addAll(database.<TYPE> queryByExample(example));
        }

        for (TYPE element : toDelete) {
            database.delete(element);
        }
        commit();
    }

    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        openDataBase(example);
        List<TYPE> result = new ArrayList<TYPE>(doQuery(example));
        commit(result);
        return result;
    }

    private <TYPE> List<TYPE> doQuery(TYPE example) {
        List<TYPE> queryByExample = database.queryByExample(example);
        return queryByExample;
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        if (examples.isEmpty()) {
            return new ArrayList<TYPE>();
        }
        openDataBase(examples.get(0));

        List<TYPE> results = new ArrayList<TYPE>();
        for (TYPE example : examples) {
            results.addAll(doQuery(example));
        }

        commit(results);
        return results;
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        openDataBase(oldBean);
        try {
            doUpdate(oldBean, newBean);
        } finally {
            commit(newBean);
        }
    }

    private <TYPE> void doUpdate(TYPE oldBean, TYPE newBean) throws PersistenceException {
        List<TYPE> queryResult = database.queryByExample(oldBean);
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
        openDataBase(beans.keySet().iterator().next());
        try {
            for (Entry<TYPE, TYPE> entry : beans.entrySet()) {
                doUpdate(entry.getKey(), entry.getValue());
            }
        } catch (PersistenceException pe) {
            database.rollback();
            throw pe;
        } finally {
            commit(beans.values());
        }
    }

    public void setDbFile(String dbFile) {
        this.dbFile = dbFile;
        Configuration config = Db4o.newConfiguration();
        jdkLoader = new SmartJdkLoader();
        config.reflectWith(new JdkReflector(jdkLoader));
        createFolder();
        database = Db4o.openFile(config, dbFile);
    }

    private void openDataBase(Object object) {
        try {
            semaphore.acquire();
            jdkLoader.addClassLoader(object.getClass().getClassLoader());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void commit() {
        commit(Collections.emptyList());
    }

    private void commit(Object o) {
        commit(Collections.singletonList(o));
    }

    private void commit(Collection<?> objects) {
        database.commit();
        for (Object object : objects) {
            database.ext().purge(object);
        }
        semaphore.release();
    }

    private void createFolder() {
        File file = new File(dbFile);
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
    }

    public void shutdown() {
        try {
            semaphore.acquire();
            database.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
