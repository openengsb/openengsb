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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.persistence.internal.NeodatisPersistenceService;
import org.openengsb.core.services.internal.CorePersistenceServiceBackend;
import org.openengsb.core.services.internal.DefaultConfigPersistenceService;
import org.openengsb.core.test.DummyPersistence;
import org.openengsb.core.workflow.internal.persistence.GlobalDeclarationPersistenceBackendService;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
import org.openengsb.core.workflow.model.GlobalDeclaration;
import org.openengsb.core.workflow.model.ImportDeclaration;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PersistenceTestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTestUtil.class);

    public static RuleManager getRuleManagerWithMockedPersistence() throws Exception {
        DummyPersistence persistence = new DummyPersistence();
        return getRuleManagerWithPersistence(persistence);
    }

    public static RuleManager getRuleManagerWithPersistenceService() throws PersistenceException, IOException,
        RuleBaseException {
        NeodatisPersistenceService persistence = createPersistence();
        return getRuleManagerWithPersistence(persistence);
    }

    @SuppressWarnings("rawtypes")
    private static RuleManager getRuleManagerWithPersistence(PersistenceService persistence) {
        PersistenceRuleManager manager = new PersistenceRuleManager();

        CorePersistenceServiceBackend backend = new CorePersistenceServiceBackend();
        backend.setPersistenceService(persistence);
        ConfigPersistenceService configService = new DefaultConfigPersistenceService(backend);
        manager.setPersistenceService(configService);

        GlobalDeclarationPersistenceBackendService globalBackend = new GlobalDeclarationPersistenceBackendService();
        FileUtils.deleteQuietly(new File("target/test/globals"));
        globalBackend.setStorageFile("target/test/globals");
        ConfigPersistenceService globalService = new DefaultConfigPersistenceService(globalBackend);
        manager.setGlobalPersistence(globalService);

        manager.init();
        return manager;
    }

    private static NeodatisPersistenceService createPersistence() throws PersistenceException, IOException {
        LOGGER.debug("creating persistence");
        final File dataFile = new File("data");
        FileUtils.deleteQuietly(dataFile);
        File refData = new File("data.ref");
        if (!refData.exists()) {
            LOGGER.debug("creating reference persistence");
            createReferencePersistence();
        }
        FileUtils.copyFile(refData, dataFile);
        NeodatisPersistenceService persistence = new NeodatisPersistenceService("data", Mockito.mock(Bundle.class));
        return persistence;
    }

    public static void createReferencePersistence() throws PersistenceException, IOException {
        FileUtils.deleteQuietly(new File("data.ref"));
        NeodatisPersistenceService persistence = new NeodatisPersistenceService("data.ref", Mockito.mock(Bundle.class));
        persistence.create(new ImportDeclaration(Event.class.getName()));
        readImports(persistence);
        readGlobals(persistence);
    }

    private static void readGlobals(PersistenceService persistence) throws IOException, PersistenceException {
        URL globalURL = ClassLoader.getSystemResource("rulebase/globals");
        File globalFile = FileUtils.toFile(globalURL);
        List<String> globalLines = FileUtils.readLines(globalFile);
        for (String s : globalLines) {
            String[] parts = s.split(" ");
            persistence.create(new GlobalDeclaration(parts[0], parts[1]));
        }
    }

    private static void readImports(PersistenceService persistence) throws IOException, PersistenceException {
        URL importsURL = ClassLoader.getSystemResource("rulebase/imports");
        File importsFile = FileUtils.toFile(importsURL);
        List<String> importLines = FileUtils.readLines(importsFile);
        for (String s : importLines) {
            persistence.create(new ImportDeclaration(s));
        }
    }

    public static void cleanup() {
        FileUtils.deleteQuietly(new File("data"));
    }

    public static void cleanupReferenceData() {
        FileUtils.deleteQuietly(new File("data.ref"));
    }

    private PersistenceTestUtil() {
    }

}
