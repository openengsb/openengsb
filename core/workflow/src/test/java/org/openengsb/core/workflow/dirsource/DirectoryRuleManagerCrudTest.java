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

package org.openengsb.core.workflow.dirsource;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openengsb.core.workflow.AbstractRuleManagerCrudTest;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;

public class DirectoryRuleManagerCrudTest extends AbstractRuleManagerCrudTest<DirectoryRuleSource> {

    public DirectoryRuleManagerCrudTest(
            List<org.openengsb.core.workflow.AbstractRuleManagerCrudTest.TestElement> testelements) {
        super(testelements);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

    @Override
    protected RuleManager getRuleBaseSource() throws RuleBaseException {
        DirectoryRuleSource source = new DirectoryRuleSource("data/rulebase");
        source.init();
        return source;
    }
}
