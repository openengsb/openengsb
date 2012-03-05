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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.persistence.internal.SerializableChecker.ObjectDbNotSerializableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DefaultPersistenceIndex implements PersistenceIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPersistenceIndex.class);
    private static final String INDEX_FILE_NAME = "index.ser";

    private File indexFile;
    private ArrayList<ObjectInfo> index = Lists.newArrayList();
    private ObjectPersistenceBackend persistenceBackend;

    public DefaultPersistenceIndex(File indexDirectory, ObjectPersistenceBackend persistenceBackend) {
        this.persistenceBackend = persistenceBackend;
        indexFile = new File(indexDirectory + "/" + INDEX_FILE_NAME);
        if (!indexFile.exists()) {
            writeIndex();
        } else {
            loadIndex();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void indexObject(Class<?> bean, File beanLocation) {
        ObjectInfo objectInfo = new ObjectInfo(bean.getName(), beanLocation.toString());
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(bean);
        for (Class<?> class1 : allInterfaces) {
            objectInfo.addType(class1);
        }
        List<Class<?>> allSuperClasses = ClassUtils.getAllSuperclasses(bean);
        for (Class<?> class1 : allSuperClasses) {
            if (isObjectClassToIgnore(class1)) {
                continue;
            }
            objectInfo.addType(class1);
        }
        objectInfo.addType(bean);
        index.add(objectInfo);
        LOGGER.debug("Adding to index {}: {}", indexFile.toString(), objectInfo.toString());
    }

    @Override
    public void removeIndexObject(ObjectInfo info) {
        index.remove(info);
    }

    @Override
    public void updateIndex() throws ObjectDbNotSerializableException {
        writeIndex();
    }

    @Override
    public List<ObjectInfo> findIndexObject(Class<?> beanClass) {
        LOGGER.trace("Looking for bean class {} in index {}", beanClass.getName(), indexFile.toString());
        List<ObjectInfo> retVal = Lists.newArrayList();
        for (ObjectInfo info : index) {
            if (info.containsClass(beanClass)) {
                retVal.add(info);
                LOGGER.debug("Looking for {} in index {} and found " + info.toString(), beanClass.getName(),
                    indexFile.toString());
            }
        }
        LOGGER.debug("Found {} objects of type bean class {} in index: " + indexFile.toString(), retVal.size(),
            beanClass.getName());
        return retVal;
    }

    private boolean isObjectClassToIgnore(Class<?> class1) {
        return class1.getName().equals(Object.class.getName());
    }

    private void writeIndex() {
        persistenceBackend.writeDatabaseObject(index, indexFile);
    }

    @SuppressWarnings("unchecked")
    private void loadIndex() {
        index = (ArrayList<ObjectInfo>) persistenceBackend.readDatabaseObject(indexFile);
    }

}
