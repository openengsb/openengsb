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

import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.test.DummyConfigPersistenceService;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
import org.openengsb.core.workflow.model.GlobalDeclaration;
import org.openengsb.core.workflow.model.ImportDeclaration;
import org.openengsb.core.workflow.model.RuleBaseElement;

/**
 * A helper class to get a Mockup RuleManager.
 * 
 */
public final class PersistenceTestUtil {

    public static RuleManager getRuleManager() throws Exception {
        PersistenceRuleManager ruleManager = new PersistenceRuleManager();
        ruleManager.setGlobalPersistence(new DefaultConfigPersistenceService(
            new DummyConfigPersistenceService<GlobalDeclaration>()));
        ruleManager.setImportPersistence(new DefaultConfigPersistenceService(
            new DummyConfigPersistenceService<ImportDeclaration>()));
        ruleManager.setRulePersistence(new DefaultConfigPersistenceService(
            new DummyConfigPersistenceService<RuleBaseElement>()));
        ruleManager.init();
        return ruleManager;
    }

    private PersistenceTestUtil() {
    }

}
