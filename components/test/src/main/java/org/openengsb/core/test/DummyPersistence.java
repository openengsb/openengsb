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

package org.openengsb.core.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceService;

public class DummyPersistence implements PersistenceService {

    private Map<Class<?>, Set<Object>> data = new HashMap<Class<?>, Set<Object>>();

    @SuppressWarnings("unchecked")
    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        Collection<Object> table = getTable(example);
        Collection<Object> result = new ArrayList<Object>();
        for (Object o : table) {
            try {
                if (matches(o, example)) {
                    result.add(o);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return (List<TYPE>) result;
    }

    private boolean matches(Object cmp, Object example) throws IllegalAccessException {
        final Class<? extends Object> objClass = example.getClass();
        for (Field f : objClass.getDeclaredFields()) {
            f.setAccessible(true);
            Object exampleValue = f.get(example);
            Object value = f.get(cmp);
            if (exampleValue != null && !value.equals(exampleValue)) {
                return false;
            }
        }
        return true;
    }

    private Set<Object> getTable(Object example) {
        final Class<? extends Object> class1 = example.getClass();
        synchronized (data) {
            if (!data.containsKey(class1)) {
                data.put(class1, new HashSet<Object>());
            }
            return data.get(class1);
        }
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        List<TYPE> result = new ArrayList<TYPE>();
        for (TYPE o : examples) {
            result.addAll(query(o));
        }
        return result;
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        getTable(bean).add(bean);
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        for (Object o : beans) {
            create(o);
        }
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        delete(oldBean);
        create(newBean);
    }

    @Override
    public <TYPE> void update(Map<TYPE, TYPE> beans) throws PersistenceException {
        for (Map.Entry<TYPE, TYPE> e : beans.entrySet()) {
            update(e.getKey(), e.getValue());
        }
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        Collection<Object> table = getTable(example);
        for (Object o : query(example)) {
            table.remove(o);
        }
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        for (TYPE o : examples) {
            delete(o);
        }
    }

}
