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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.ProcessBag;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.api.workflow.model.Task;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TaskboxIT extends AbstractPreConfiguredExamTestHelper {
    private TaskboxService taskboxService;
    private WorkflowService workflowService;
    private RuleManager ruleManager;

    @Before
    public void setUp() throws Exception {
        ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
        if (!contextService.getAvailableContexts().contains("it-taskbox")) {
            contextService.createContext("it-taskbox");
        }
        ContextHolder.get().setCurrentContextId("it-taskbox");
        ruleManager = getOsgiService(RuleManager.class);
        workflowService = getOsgiService(WorkflowService.class);
        taskboxService = getOsgiService(TaskboxService.class);
    }

    @Test
    public void testHumanTaskFlow_shouldWorkWithGivenProcessBag() throws WorkflowException, IOException,
        RuleBaseException {
        addWorkflow("TaskDemoWorkflow");

        ProcessBag processBag = new ProcessBag();
        processBag.addProperty("test", "test");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("processBag", processBag);

        assertThat(taskboxService.getOpenTasks().size(), is(0));

        workflowService.startFlow("TaskDemoWorkflow", parameterMap);
        System.out.println("workflow started, getting processId");
        assertNotNull(processBag.getProcessId());
        System.out.println("got processId");
        assertThat(taskboxService.getOpenTasks().size(), is(1));
        System.out.println("opentasks is 1");

        Task task = taskboxService.getOpenTasks().get(0);
        System.out.println("got task");
        assertEquals(task.getProcessId(), processBag.getProcessId());
        assertEquals(task.getProperty("test"), "test");
        assertEquals(task.getTaskType(), "demo");
        assertNotNull(task.getTaskId());
        System.out.println("task correct, finishing");
        taskboxService.finishTask(task);
        assertTrue(taskboxService.getOpenTasks().size() == 0);
    }

    @Test
    public void testHumanTaskFlow_shouldCreateOwnProcessBag() throws WorkflowException, IOException, RuleBaseException {
        addWorkflow("TaskDemoWorkflow");

        assertThat(taskboxService.getOpenTasks().size(), is(0));

        workflowService.startFlow("TaskDemoWorkflow");
        assertThat(taskboxService.getOpenTasks().size(), is(1));

        Task task = taskboxService.getOpenTasks().get(0);
        assertNotNull(task.getProcessId());
        assertNotNull(task.getTaskId());

        taskboxService.finishTask(task);
        assertThat(taskboxService.getOpenTasks().size(), is(0));
    }

    @Test
    public void testHumanTaskFlow_shouldHandleMultipleTasks() throws WorkflowException, IOException, RuleBaseException {
        addWorkflow("TaskDemoWorkflow");

        assertThat(taskboxService.getOpenTasks().size(), is(0));

        long id = workflowService.startFlow("TaskDemoWorkflow");
        long id2 = workflowService.startFlow("TaskDemoWorkflow");

        assertThat(taskboxService.getOpenTasks().size(), is(2));

        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id)).size(), 1);
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id2)).size(), 1);

        Task task1 = taskboxService.getTasksForProcessId(String.valueOf(id)).get(0);
        Task task2 = taskboxService.getTasksForProcessId(String.valueOf(id2)).get(0);

        taskboxService.finishTask(task1);
        assertThat(taskboxService.getOpenTasks().size(), is(1));
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id)).size(), 0);
        assertEquals(taskboxService.getTasksForProcessId(String.valueOf(id2)).size(), 1);

        taskboxService.finishTask(task2);
        assertThat(taskboxService.getOpenTasks().size(), is(0));
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

        task.setName("test");
        taskboxService.updateTask(task);
        task = taskboxService.getOpenTasks().get(0);
        assertEquals("test", task.getName());

        taskboxService.finishTask(task);

        task = taskboxService.getOpenTasks().get(0);
        assertEquals(task.getProperty("test"), date);
        assertEquals(task.getTaskType(), "step2");

        taskboxService.finishTask(task);
        assertThat(taskboxService.getOpenTasks().size(), is(0));
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
