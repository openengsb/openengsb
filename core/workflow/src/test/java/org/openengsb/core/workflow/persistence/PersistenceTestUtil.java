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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.persistence.internal.NeodatisPersistenceService;
import org.openengsb.core.workflow.internal.persistence.GlobalDeclaration;
import org.openengsb.core.workflow.internal.persistence.ImportDeclaration;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;

public final class PersistenceTestUtil {

    public static RuleManager getRuleManager() throws Exception {
        PersistenceRuleManager manager = new PersistenceRuleManager();
        NeodatisPersistenceService persistence = createPersistence();
        manager.setPersistence(persistence);
        manager.init();
        return manager;
    }

    private static NeodatisPersistenceService createPersistence() throws PersistenceException, IOException {
        FileUtils.deleteQuietly(new File("data"));
        NeodatisPersistenceService persistence =
            new NeodatisPersistenceService("data", ClassLoader.getSystemClassLoader());
        persistence.create(new ImportDeclaration(Event.class.getName()));
        readImports(persistence);
        readGlobals(persistence);
        return persistence;
    }

    private static void readGlobals(PersistenceService persistence) throws IOException, PersistenceException {
        URL globalURL = ClassLoader.getSystemResource("rulebase/globals");
        File globalFile = FileUtils.toFile(globalURL);
        @SuppressWarnings("unchecked")
        List<String> globalLines = FileUtils.readLines(globalFile);
        for (String s : globalLines) {
            String[] parts = s.split(" ");
            persistence.create(new GlobalDeclaration(parts[0], parts[1]));
        }
    }

    private static void readImports(PersistenceService persistence) throws IOException, PersistenceException {
        URL importsURL = ClassLoader.getSystemResource("rulebase/imports");
        File importsFile = FileUtils.toFile(importsURL);
        @SuppressWarnings("unchecked")
        List<String> importLines = FileUtils.readLines(importsFile);
        for (String s : importLines) {
            persistence.create(new ImportDeclaration(s));
        }
    }

    public static void cleanup() throws Exception {
        FileUtils.deleteQuietly(new File("data"));
    }

    private PersistenceTestUtil() {
    }

}
