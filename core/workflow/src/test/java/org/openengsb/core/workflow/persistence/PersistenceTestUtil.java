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

import org.apache.commons.io.FileUtils;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.persistence.internal.NeodatisPersistenceService;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;

public final class PersistenceTestUtil {

    public static RuleManager getRuleManager() throws Exception {
        FileUtils.deleteQuietly(new File("data"));
        PersistenceRuleManager m = new PersistenceRuleManager();
        PersistenceService service = new NeodatisPersistenceService("data", ClassLoader.getSystemClassLoader());
        m.setPersistence(service);
        m.init();
        return m;
    }

    public static void cleanup() throws Exception {
        FileUtils.deleteQuietly(new File("data"));
    }

    private PersistenceTestUtil() {
    }

}
