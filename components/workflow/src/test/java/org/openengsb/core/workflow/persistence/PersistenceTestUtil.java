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

package org.openengsb.core.workflow.persistence;

import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.services.internal.DefaultConfigPersistenceService;
import org.openengsb.core.workflow.internal.persistence.GlobalDeclarationPersistenceBackendService;
import org.openengsb.core.workflow.internal.persistence.ImportDeclarationPersistenceBackendService;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
import org.openengsb.core.workflow.internal.persistence.RuleBaseElementPersistenceBackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PersistenceTestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTestUtil.class);

    public static RuleManager getRuleManager(TemporaryFolder tempFolder) throws Exception {
        PersistenceRuleManager manager = new PersistenceRuleManager();

        GlobalDeclarationPersistenceBackendService globalBackend = new GlobalDeclarationPersistenceBackendService();
        globalBackend.setStorageFilePath(tempFolder.newFile("globals").getPath());
        ConfigPersistenceService globalService = new DefaultConfigPersistenceService(globalBackend);
        manager.setGlobalPersistence(globalService);

        ImportDeclarationPersistenceBackendService importBackend = new ImportDeclarationPersistenceBackendService();
        importBackend.setStorageFilePath(tempFolder.newFile("imports").getPath());
        ConfigPersistenceService importService = new DefaultConfigPersistenceService(importBackend);
        manager.setImportPersistence(importService);

        RuleBaseElementPersistenceBackendService ruleBackend = new RuleBaseElementPersistenceBackendService();
        ruleBackend.setStorageFolderPath(tempFolder.newFolder("flows").getPath());
        try {
            ruleBackend.init();
        } catch (PersistenceException e) {
            LOGGER.error(e.getMessage());
        }
        ConfigPersistenceService ruleService = new DefaultConfigPersistenceService(ruleBackend);
        manager.setRuleService(ruleService);

        manager.init();
        return manager;
    }

    private PersistenceTestUtil() {
    }

}
