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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.common.workflow.model.ProcessBag;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class TaskboxIT extends AbstractExamTestHelper {
    private TaskboxService taskboxService;
    private WorkflowService workflowService;

    @Before
    public void setUp() throws Exception {
        ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
        contextService.createContext("it-taskbox");
        contextService.setThreadLocalContext("it-taskbox");

        taskboxService = getOsgiService(TaskboxService.class);
        ruleManager = getOsgiService(RuleManager.class);
    }

    @After
    public void cleanup() throws IOException {
        super.afterClass();
    }

    @Test
    public void testHumanTaskFlow_shouldWorkWithGivenProcessBag() throws WorkflowException {
        ProcessBag processBag = new ProcessBag();
        processBag.addProperty("test", "test");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("processBag", processBag);

        assertTrue(taskboxService.getOpenTasks().size() == 0);

        workflowService.startFlow("humantask", parameterMap);

        assertNotNull(processBag.getProcessId());
        assertTrue(taskboxService.getOpenTasks().size() == 1);

        Task task = taskboxService.getOpenTasks().get(0);
        assertEquals(task.getProcessId(), processBag.getProcessId());
        assertEquals(task.getProperty("test"), "test");
        assertNotNull(task.getTaskId());

        taskboxService.finishTask(task);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
    }

    @Ignore
    @Test
    public void testHumanTaskFlow_shouldCreateOwnProcessBag() throws WorkflowException {
        assertTrue(taskboxService.getOpenTasks().size() == 0);

        workflowService.startFlow("humantask");
        assertTrue(taskboxService.getOpenTasks().size() == 1);

        Task task = taskboxService.getOpenTasks().get(0);
        assertNotNull(task.getProcessId());
        assertNotNull(task.getTaskId());

        taskboxService.finishTask(task);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
    }

    @Ignore
    @Test
    public void testHumanTaskFlow_shouldHandleMultipleTasks() throws WorkflowException {
        assertTrue(taskboxService.getOpenTasks().size() == 0);

        workflowService.startFlow("humantask");
        workflowService.startFlow("humantask");
        workflowService.startFlow("humantask");
        assertTrue(taskboxService.getOpenTasks().size() == 3);

        taskboxService.finishTask(taskboxService.getOpenTasks().get(0));
        assertTrue(taskboxService.getOpenTasks().size() == 2);
    }
}
