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
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.common.workflow.model.ProcessBag;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class TaskboxIT extends AbstractExamTestHelper {
    private TaskboxService taskboxService;
    private WorkflowService workflowService;
    private RuleManager ruleManager;

    @Before
    public void setUp() throws Exception {
        super.beforeClass();

        ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
        if (!contextService.getAvailableContexts().contains("it-taskbox")) {
            contextService.createContext("it-taskbox");
            contextService.setThreadLocalContext("it-taskbox");
            contextService.putValue("domain/AuditingDomain/defaultConnector/id", "auditing");
        } else {
            contextService.setThreadLocalContext("it-taskbox");
        }

        ruleManager = getOsgiService(RuleManager.class);
        workflowService = getOsgiService(WorkflowService.class);
        taskboxService = getOsgiService(TaskboxService.class, 30000);
    }

    @Test
    public void testHumanTaskFlow_shouldWorkWithGivenProcessBag() throws WorkflowException, IOException,
        RuleBaseException {
        addWorkflow("TaskDemoWorkflow");

        ProcessBag processBag = new ProcessBag();
        processBag.addProperty("test", "test");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("processBag", processBag);

        assertTrue(taskboxService.getOpenTasks().size() == 0);

        workflowService.startFlow("TaskDemoWorkflow", parameterMap);

        assertNotNull(processBag.getProcessId());
        assertTrue(taskboxService.getOpenTasks().size() == 1);

        Task task = taskboxService.getOpenTasks().get(0);
        assertEquals(task.getProcessId(), processBag.getProcessId());
        assertEquals(task.getProperty("test"), "test");
        assertEquals(task.getTaskType(), "demo");
        assertNotNull(task.getTaskId());

        taskboxService.finishTask(task);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
    }

    @Test
    public void testHumanTaskFlow_shouldCreateOwnProcessBag() throws WorkflowException, IOException, RuleBaseException {
        addWorkflow("TaskDemoWorkflow");

        assertTrue(taskboxService.getOpenTasks().size() == 0);

        workflowService.startFlow("TaskDemoWorkflow");
        assertTrue(taskboxService.getOpenTasks().size() == 1);

        Task task = taskboxService.getOpenTasks().get(0);
        assertNotNull(task.getProcessId());
        assertNotNull(task.getTaskId());

        taskboxService.finishTask(task);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
    }

    @Test
    public void testHumanTaskFlow_shouldHandleMultipleTasks() throws WorkflowException, IOException, RuleBaseException,
        TaskboxException {
        addWorkflow("TaskDemoWorkflow");

        assertTrue(taskboxService.getOpenTasks().size() == 0);

        long id = workflowService.startFlow("TaskDemoWorkflow");
        long id2 = workflowService.startFlow("TaskDemoWorkflow");

        assertTrue(taskboxService.getOpenTasks().size() == 2);

        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id)).size(), 1);
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id2)).size(), 1);

        Task task1 = taskboxService.getTasksForProcessId(String.valueOf(id)).get(0);
        Task task2 = taskboxService.getTasksForProcessId(String.valueOf(id2)).get(0);

        taskboxService.finishTask(task1);
        assertTrue(taskboxService.getOpenTasks().size() == 1);
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id)).size(), 0);
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id2)).size(), 1);

        taskboxService.finishTask(task2);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id2)).size(), 0);
    }

    @Test
    public void testCompleteWorkflow_humanInteractionShouldReplaceValues() throws WorkflowException, IOException,
        RuleBaseException, InterruptedException {
        addWorkflow("HIDemoWorkflow");

        workflowService.startFlow("HIDemoWorkflow");
        Task task = taskboxService.getOpenTasks().get(0);
        Date date = new Date();
        task.addOrReplaceProperty("test", date);
        assertEquals(task.getTaskType(), "step1");

        taskboxService.finishTask(task);

        task = taskboxService.getOpenTasks().get(0);
        assertEquals(task.getProperty("test"), date);
        assertEquals(task.getTaskType(), "step2");

        taskboxService.finishTask(task);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
    }

    private void addWorkflow(String workflow) throws IOException, RuleBaseException {
        if (ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, workflow)) == null) {
            InputStream is =
                getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + workflow + ".rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, workflow);
            ruleManager.add(id, testWorkflow);
            IOUtils.closeQuietly(is);
        }
    }
}
