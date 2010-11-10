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

import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.taskbox.TaskboxException;
import org.openengsb.core.taskbox.TaskboxService;
import org.openengsb.core.taskbox.model.Ticket;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
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
        contextService.createContext("events");
        contextService.setThreadLocalContext("events");
        ruleManager.addGlobal(TaskboxService.class.getCanonicalName(), "taskbox");

        InputStream is = getClass().getClassLoader().getResourceAsStream("eventtest.rf");
        String testWorkflow = IOUtils.toString(is);
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, "eventtest");
        ruleManager.add(id, testWorkflow);
    }

    @Test
    public void eventTest() throws TaskboxException, WorkflowException {
        UUID uuid = UUID.randomUUID();
        Ticket ticket = new Ticket("ID-" + uuid.toString());

        taskboxService.startWorkflow("eventtest", "task", ticket);

        taskboxService.processEvent(new Event() {
            @Override
            public String getType() {
                return "FirstClick";
            }
        });
        assertEquals(taskboxService.getWorkflowMessage(), "First step finished");

        taskboxService.processEvent(new Event() {
            @Override
            public String getType() {
                return "SecondClick";
            }
        });
        assertEquals(taskboxService.getWorkflowMessage(), "Second step finished");

        taskboxService.processEvent(new Event() {
            @Override
            public String getType() {
                return "ThirdClick";
            }
        });
        assertEquals(taskboxService.getWorkflowMessage(), "Third step finished");
    }
}
