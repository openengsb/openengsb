/**OEYYPE
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
import java.util.Map.Entry;

import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;

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
        database.commit();
        database.ext().purge(bean);
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        for (Object bean : beans) {
            database.store(bean);
        }
        database.commit();
        for (Object bean : beans) {
            database.ext().purge(bean);
        }
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        List<TYPE> toDelete = database.queryByExample(example);
        if (toDelete.isEmpty()) {
            throw new PersistenceException("Element '" + example
                    + "' cannot be deleted because it was not found in database.");
        }
        for (TYPE element : toDelete) {
            database.delete(element);
        }
        database.commit();
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        List<TYPE> toDelete = new ArrayList<TYPE>();

        for (TYPE example : examples) {
            toDelete.addAll(database.<TYPE> queryByExample(example));
        }

        for (TYPE element : toDelete) {
            database.delete(element);
        }
        database.commit();
    }

    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        List<TYPE> queryByExample = database.queryByExample(example);
        List<TYPE> result = new ArrayList<TYPE>();
        for (TYPE element : queryByExample) {
            database.ext().purge(element);
            result.add(element);
        }
        return result;
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        List<TYPE> results = new ArrayList<TYPE>();
        for (TYPE example : examples) {
            results.addAll(query(example));
        }
        return results;
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        doUpdate(oldBean, newBean);
        database.commit();
        database.ext().purge(newBean);
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
        try {
            for (Entry<TYPE, TYPE> entry : beans.entrySet()) {
                doUpdate(entry.getKey(), entry.getValue());
                database.ext().purge(entry.getValue());
            }
        } catch (PersistenceException pe) {
            database.rollback();
            throw pe;
        }
        database.commit();
    }

    public void setDbFile(String dbFile) {
        this.dbFile = dbFile;
    }

}
