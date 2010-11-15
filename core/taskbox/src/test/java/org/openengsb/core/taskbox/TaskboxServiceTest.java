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

package org.openengsb.core.taskbox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.taskbox.model.Task;

public class TaskboxServiceTest {
    private TaskboxServiceImpl service;
    private WorkflowService workflowService;

    @Before
    public void init() throws Exception {
        workflowService = mock(WorkflowService.class);

        service = new TaskboxServiceImpl();
        service.setWorkflowService(workflowService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartWorkflow() throws TaskboxException, WorkflowException {
        Task t = null;
        service.startWorkflow("tasktest", "ticket", t);

        verify(workflowService, Mockito.times(1)).startFlow(Mockito.anyString(), Mockito.anyMap());
    }

    @Test(expected = TaskboxException.class)
    public void testGetEmptyWorkflowMessage() throws TaskboxException {
        service.getWorkflowMessage();
    }

    @Test
    public void testWorkflowMessage() throws TaskboxException {
        service.setWorkflowMessage("testmessage");
        assertEquals("testmessage", service.getWorkflowMessage());
    }

}
