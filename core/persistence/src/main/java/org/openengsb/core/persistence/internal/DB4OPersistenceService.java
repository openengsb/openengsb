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
import java.util.List;
import java.util.Map;

import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

public class DB4OPersistenceService implements PersistenceService {

    private String dbFile;

    private ObjectContainer database;

    public void init() {
        database = Db4o.openFile(dbFile);
    }

    public void shutdown() {
        database.close();
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        database.store(bean);
        database.ext().purge(bean);
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        for (Object bean : beans) {
            database.store(bean);
        }
        for (Object bean : beans) {
            database.ext().purge(bean);
        }
    }

    @Override
    public void delete(Object example) throws PersistenceException {
    }

    @Override
    public void delete(List<? extends Object> examples) throws PersistenceException {
    }

    @Override
    public <T> List<T> query(T example) {
        ObjectSet<T> queryByExample = database.queryByExample(example);
        List<T> result = new ArrayList<T>();
        for (T element : queryByExample) {
            database.ext().purge(element);
            result.add(element);
        }
        return result;
    }

    @Override
    public <T> List<T> query(List<T> examples) {
        return null;
    }

    @Override
    public void update(Object oldBean, Object newBean) throws PersistenceException {
        // TODO not yet implemented
    }

    @Override
    public void update(Map<? extends Object, ? extends Object> beans) throws PersistenceException {
        // TODO not yet implemented

    }

    public void setDbFile(String dbFile) {
        this.dbFile = dbFile;
    }

}
