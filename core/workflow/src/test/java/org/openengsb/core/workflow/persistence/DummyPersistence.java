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
package org.openengsb.core.workflow.persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceService;

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
        if (!data.containsKey(class1)) {
            data.put(class1, new HashSet<Object>());
        }
        return data.get(class1);
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        getTable(bean).add(bean);
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        delete(oldBean);
        create(newBean);
    }

    @Override
    public <TYPE> void update(Map<TYPE, TYPE> beans) throws PersistenceException {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

}
