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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.osgi.Db4oService;

public class DB4OPersistenceService implements PersistenceService {

    private Db4oService db4oService;

    private String dbFile;

    private ObjectContainer database;

    public void init() {
        database = db4oService.openFile(getDbFile());
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        database.store(bean);
    }

    @Override
    public void create(List<Object> beans) throws PersistenceException {
        for (Object bean : beans) {
            create(bean);
        }
    }

    @Override
    public void delete(Object example) throws PersistenceException {
        ObjectSet queryByExample = database.queryByExample(example);
        for (Object bean : queryByExample) {
            database.delete(bean);
        }
    }

    @Override
    public void delete(List<Object> examples) throws PersistenceException {
        for (Object example : examples) {
            delete(example);
        }
    }

    @Override
    public List<Object> query(Object example) {
        ObjectSet queryResult = database.queryByExample(example);
        return toList(queryResult);
    }

    @Override
    public List<Object> query(List<Object> examples) {
        Set<Object> result = new HashSet<Object>();
        for (Object example : examples) {
            ObjectSet queryResult = database.queryByExample(example);
            result.addAll(toSet(queryResult));
        }
        return new ArrayList<Object>(result);
    }

    @Override
    public void update(Object oldBean, Object newBean) throws PersistenceException {
        // TODO not yet implemented
    }

    @Override
    public void update(Map<Object, Object> beans) throws PersistenceException {
        // TODO not yet implemented

    }

    private List<Object> toList(ObjectSet set) {
        List<Object> result = new ArrayList<Object>();
        for (Object o : set) {
            result.add(o);
        }
        return result;
    }

    private Set<Object> toSet(ObjectSet set) {
        Set<Object> result = new HashSet<Object>();
        for (Object o : set) {
            result.add(o);
        }
        return result;
    }

    public Db4oService getDb4oService() {
        return db4oService;
    }

    public void setDb4oService(Db4oService db4oService) {
        this.db4oService = db4oService;
    }

    public void setDbFile(String dbFile) {
        this.dbFile = dbFile;
    }

    public String getDbFile() {
        return dbFile;
    }

}
