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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DefaultPersistenceService implements PersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPersistenceService.class);

    private final File storageLocation;
    private final PersistenceIndex index;
    private final ObjectPersistenceBackend persitenceBackend;

    public DefaultPersistenceService(File storageLocation, ObjectPersistenceBackend persitenceBackend,
            PersistenceIndex index) {
        this.storageLocation = storageLocation;
        this.persitenceBackend = persitenceBackend;
        this.index = index;
    }

    @Override
    public void create(Object bean) throws PersistenceException {
        synchronized (index) {
            indexBean(bean);
            index.updateIndex();
        }
    }

    @Override
    public void create(List<? extends Object> beans) throws PersistenceException {
        synchronized (index) {
            for (Object bean : beans) {
                indexBean(bean);
            }
            index.updateIndex();
        }
    }

    private void indexBean(Object bean) {
        File objectFile = getNewObjectFile();
        persitenceBackend.writeDatabaseObject(bean, objectFile);
        index.indexObject(bean.getClass(), objectFile);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TYPE> List<TYPE> query(TYPE example) {
        List<ObjectInfo> objects = index.findIndexObject(example.getClass());
        List<TYPE> finalResult = Lists.newArrayList();
        for (ObjectInfo info : objects) {
            Object databaseObject = persitenceBackend.readDatabaseObject(new File(info.getLocation()));
            if (databaseObject == null) {
                continue;
            }
            if (evaluateObject(example, databaseObject)) {
                finalResult.add((TYPE) databaseObject);
            }
        }
        LOGGER.debug("Reduced {} objects to final result of size {}", objects.size(), finalResult.size());
        return finalResult;
    }

    private boolean evaluateObject(Object example, Object databaseObject) {
        return example.equals(databaseObject);
    }

    @Override
    public <TYPE> List<TYPE> query(List<TYPE> examples) {
        List<TYPE> allResults = Lists.newArrayList();
        for (TYPE example : examples) {
            allResults.addAll(query(example));
        }
        return allResults;
    }

    @Override
    public <TYPE> void update(TYPE oldBean, TYPE newBean) throws PersistenceException {
        updateObject(oldBean, newBean);
        index.updateIndex();
    }

    private <TYPE> void updateObject(TYPE oldBean, TYPE newBean) throws PersistenceException {
        List<ObjectInfo> objects = index.findIndexObject(oldBean.getClass());
        List<ObjectInfo> toRemove = Lists.newArrayList();
        for (ObjectInfo info : objects) {
            Object databaseObject = persitenceBackend.readDatabaseObject(new File(info.getLocation()));
            if (evaluateObject(oldBean, databaseObject)) {
                LOGGER.info("Found object matching all equals of old bean {}", oldBean.getClass());
                toRemove.add(info);
            }
        }
        if (toRemove.size() != 1) {
            throw new PersistenceException("No unique object to remove available.");
        }
        for (ObjectInfo info : toRemove) {
            index.removeIndexObject(info);
            new File(info.getLocation()).delete();
            indexBean(newBean);
        }
    }

    @Override
    public <TYPE> void update(Map<TYPE, TYPE> beans) throws PersistenceException {
        Set<TYPE> keySet = beans.keySet();
        for (TYPE key : keySet) {
            TYPE value = beans.get(key);
            updateObject(key, value);
        }
        index.updateIndex();
    }

    @Override
    public <TYPE> void delete(TYPE example) throws PersistenceException {
        deleteObject(example);
        index.updateIndex();
    }

    @Override
    public <TYPE> void delete(List<? extends TYPE> examples) throws PersistenceException {
        for (TYPE example : examples) {
            deleteObject(example);
        }
        index.updateIndex();
    }

    private <TYPE> void deleteObject(TYPE example) {
        List<ObjectInfo> objects = index.findIndexObject(example.getClass());
        boolean foundObjectToDelete = false;
        for (ObjectInfo info : objects) {
            Object databaseObject = persitenceBackend.readDatabaseObject(new File(info.getLocation()));
            if (evaluateObject(example, databaseObject)) {
                LOGGER.info("Found object matching all equals of bean to remvoe{}", example.getClass());
                index.removeIndexObject(info);
                new File(info.getLocation()).delete();
                foundObjectToDelete = true;
            }
        }
        if (!foundObjectToDelete) {
            throw new PersistenceException("No element could be found to be deleted");
        }
    }

    private File getNewObjectFile() {
        return new File(storageLocation + "/" + UUID.randomUUID() + ".ser");
    }

}
