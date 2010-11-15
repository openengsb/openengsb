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

package org.openengsb.integrationtest.exam;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.taskbox.TaskboxException;
import org.openengsb.core.taskbox.TaskboxService;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class TaskboxIT extends AbstractExamTestHelper {
    private TaskboxService taskboxService;
    private RuleManager ruleManager;

    @Before
    public void setUp() throws Exception {
        taskboxService = retrieveService(getBundleContext(), TaskboxService.class);
        ruleManager = retrieveService(getBundleContext(), RuleManager.class);
        ContextCurrentService contextService = retrieveService(getBundleContext(), ContextCurrentService.class);
        contextService.createContext("it-taskbox");
        contextService.setThreadLocalContext("it-taskbox");
        ruleManager.addGlobal(TaskboxService.class.getCanonicalName(), "taskbox");
    }

    @Test
    public void test_Name() throws TaskboxException, WorkflowException {

    }
}
