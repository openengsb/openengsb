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
import org.openengsb.core.api.Event;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.persistence.internal.DefaultObjectPersistenceBackend;
import org.openengsb.core.persistence.internal.DefaultPersistenceIndex;
import org.openengsb.core.persistence.internal.DefaultPersistenceService;
import org.openengsb.core.test.DummyPersistence;
import org.openengsb.core.workflow.internal.DroolsRuleManager;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
import org.openengsb.core.workflow.model.GlobalDeclaration;
import org.openengsb.core.workflow.model.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PersistenceTestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTestUtil.class);

    public static DroolsRuleManager getRuleManagerWithMockedPersistence() throws Exception {
        PersistenceRuleManager manager = new PersistenceRuleManager();
        DummyPersistence persistenceMock = new DummyPersistence();
        manager.setPersistence(persistenceMock);
        return manager;
    }

    public static DroolsRuleManager getRuleManager() throws Exception {
        return getRuleManagerWithMockedPersistence();
    }

    public static DroolsRuleManager getRuleManagerWithPersistenceService() throws PersistenceException, IOException,
        RuleBaseException {
        PersistenceRuleManager manager = new PersistenceRuleManager();
        DefaultPersistenceService persistence = createPersistence();
        manager.setPersistence(persistence);
        manager.init();
        return manager;
    }

    private static DefaultPersistenceService createPersistence() throws PersistenceException, IOException {
        LOGGER.debug("creating persistence");
        final File dataFile = new File("data");
        FileUtils.deleteQuietly(dataFile);
        File refData = new File("data.ref");
        if (!refData.exists()) {
            LOGGER.debug("creating reference persistence");
            createReferencePersistence();
        }
        FileUtils.copyDirectory(refData, dataFile);
        DefaultPersistenceService persistence =
            new DefaultPersistenceService(new File("data"), new DefaultObjectPersistenceBackend(),
                new DefaultPersistenceIndex(new File("data"), new DefaultObjectPersistenceBackend()));
        return persistence;
    }

    public static void createReferencePersistence() throws PersistenceException, IOException {
        FileUtils.deleteQuietly(new File("data.ref"));
        new File("data.ref").mkdirs();
        DefaultPersistenceService persistence =
            new DefaultPersistenceService(new File("data.ref"), new DefaultObjectPersistenceBackend(),
                new DefaultPersistenceIndex(new File("data.ref"), new DefaultObjectPersistenceBackend()));
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
