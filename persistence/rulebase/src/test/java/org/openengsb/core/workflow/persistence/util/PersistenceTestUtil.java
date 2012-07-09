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

package org.openengsb.core.workflow.persistence.util;

import java.io.IOException;

import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
import org.openengsb.persistence.rulebase.filebackend.GlobalDeclarationPersistenceBackendService;
import org.openengsb.persistence.rulebase.filebackend.ImportDeclarationPersistenceBackendService;
import org.openengsb.persistence.rulebase.filebackend.RuleBaseElementPersistenceBackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PersistenceTestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTestUtil.class);

    public static RuleManager getRuleManager(TemporaryFolder tempFolder) throws Exception {
        PersistenceRuleManager manager = new PersistenceRuleManager();

        ConfigPersistenceService globalService = getGlobalConfigPersistence(tempFolder);
        manager.setGlobalPersistence(globalService);

        ConfigPersistenceService importService = getImportConfigPersistence(tempFolder);
        manager.setImportPersistence(importService);

        ConfigPersistenceService ruleService = getRuleConfigPersistence(tempFolder);
        manager.setRulePersistence(ruleService);

        manager.init();

        return manager;
    }

    private static ConfigPersistenceService getGlobalConfigPersistence(TemporaryFolder tempFolder) throws IOException {
        GlobalDeclarationPersistenceBackendService globalBackend = new GlobalDeclarationPersistenceBackendService();
        globalBackend.setStorageFilePath(tempFolder.newFile("globals").getPath());
        return new DefaultConfigPersistenceService(globalBackend);
    }

    private static ConfigPersistenceService getImportConfigPersistence(TemporaryFolder tempFolder) throws IOException {
        ImportDeclarationPersistenceBackendService importBackend = new ImportDeclarationPersistenceBackendService();
        importBackend.setStorageFilePath(tempFolder.newFile("imports").getPath());
        return new DefaultConfigPersistenceService(importBackend);
    }

    private static ConfigPersistenceService getRuleConfigPersistence(TemporaryFolder tempFolder) throws IOException {
        RuleBaseElementPersistenceBackendService ruleBackend = new RuleBaseElementPersistenceBackendService();
        ruleBackend.setStorageFolderPath(tempFolder.newFolder("flows").getPath());
        try {
            ruleBackend.init();
        } catch (PersistenceException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return new DefaultConfigPersistenceService(ruleBackend);
    }

    private PersistenceTestUtil() {
    }

}
