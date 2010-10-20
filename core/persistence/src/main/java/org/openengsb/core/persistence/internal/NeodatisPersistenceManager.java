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
import java.util.HashMap;
import java.util.Map;

import org.neodatis.odb.OdbConfiguration;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
import org.osgi.framework.Bundle;

public class NeodatisPersistenceManager implements PersistenceManager {

    private String persistenceRootDir;

    private Map<String, PersistenceService> persistenceServices = new HashMap<String, PersistenceService>();

    public NeodatisPersistenceManager() {
        OdbConfiguration.useMultiThread(true);
    }

    @Override
    public synchronized PersistenceService getPersistenceForBundle(Bundle bundle) {
        checkRootDirCreated();
        String dbFile = persistenceRootDir + "/" + getFileName(bundle);
        if (persistenceServices.containsKey(dbFile)) {
            return persistenceServices.get(dbFile);
        }
        CustomClassLoader classLoader = new CustomClassLoader(getClass().getClassLoader(), bundle);
        PersistenceService bundleService = new NeodatisPersistenceService(dbFile, classLoader);
        persistenceServices.put(dbFile, bundleService);
        return bundleService;
    }

    private void checkRootDirCreated() {
        File rootDir = new File(persistenceRootDir);
        if (rootDir.exists()) {
            return;
        }
        rootDir.mkdirs();
    }

    private String getFileName(Bundle bundle) {
        return bundle.getSymbolicName().hashCode() + "_" + bundle.getVersion().hashCode() + ".data";
    }

    public void setPersistenceRootDir(String persistenceRootDir) {
        this.persistenceRootDir = persistenceRootDir;
    }

}
