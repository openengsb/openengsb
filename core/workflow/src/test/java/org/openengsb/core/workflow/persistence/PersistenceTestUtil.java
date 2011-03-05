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

package org.openengsb.core.workflow.persistence;

import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.test.DummyPersistence;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;

public final class PersistenceTestUtil {

    public static RuleManager getRuleManagerWithMockedPersistence() throws Exception {
        PersistenceRuleManager manager = new PersistenceRuleManager();
        DummyPersistence persistenceMock = new DummyPersistence();
        manager.setPersistence(persistenceMock);
        manager.init();
        return manager;
    }

    public static RuleManager getRuleManager() throws Exception {
        return getRuleManagerWithMockedPersistence();
    }

    private PersistenceTestUtil() {
    }

}
