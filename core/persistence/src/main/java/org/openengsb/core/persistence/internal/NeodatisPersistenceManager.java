/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.core.persistence.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neodatis.odb.OdbConfiguration;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.osgi.framework.Bundle;

public class NeodatisPersistenceManager implements PersistenceManager {

    private final Log log = LogFactory.getLog(NeodatisPersistenceManager.class);

    private String persistenceRootDir;

    private final Map<String, PersistenceService> persistenceServices = new HashMap<String, PersistenceService>();

    public NeodatisPersistenceManager() {
        OdbConfiguration.useMultiThread(true);
    }

    @Override
    public synchronized PersistenceService getPersistenceForBundle(Bundle bundle) {
        checkRootDirCreated();
        String dbFile = new File(getAbsoluteRootDir(), getFileName(bundle)).getPath();
        if (persistenceServices.containsKey(dbFile)) {
            return persistenceServices.get(dbFile);
        }
        PersistenceService bundleService = new NeodatisPersistenceService(dbFile, bundle);
        persistenceServices.put(dbFile, bundleService);
        return bundleService;
    }

    private void checkRootDirCreated() {
        File rootDir = getAbsoluteRootDir();
        if (rootDir.exists()) {
            return;
        }
        rootDir.mkdirs();
    }

    private File getAbsoluteRootDir() {
        String karafData = System.getProperty("karaf.data");
        log.info("karafData: " + karafData);
        return new File(karafData, persistenceRootDir);
    }

    private String getFileName(Bundle bundle) {
        final String name = bundle.getSymbolicName() + ".data";
        log.info("generated persitence-filename: " + name);
        return name;
    }

    public void setPersistenceRootDir(String persistenceRootDir) {
        this.persistenceRootDir = persistenceRootDir;
    }

}
