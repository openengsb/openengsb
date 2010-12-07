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
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class TaskboxServiceTest {
    private TaskboxServiceImpl service;
    private PersistenceService persistenceService;
    private WorkflowService workflowService;
    private TaskboxServiceInternalImpl internalService;

    @Before
    public void init() throws Exception {
        workflowService = mock(WorkflowService.class);
        persistenceService = mock(PersistenceService.class);
        PersistenceManager persistenceManager = mock(PersistenceManager.class);
        when(persistenceManager.getPersistenceForBundle(any(Bundle.class))).thenReturn(persistenceService);

        service = new TaskboxServiceImpl();
        service.setBundleContext(mock(BundleContext.class));
        service.setWorkflowService(workflowService);

        internalService = new TaskboxServiceInternalImpl();
        internalService.setBundleContext(mock(BundleContext.class));
        internalService.setPersistenceManager(persistenceManager);
        internalService.init();

        service.setPersistenceManager(persistenceManager);
        service.init();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartWorkflow_shouldStartOneWorkflow() throws TaskboxException, WorkflowException {
        service.startWorkflow("tasktest", "ticket", null);

        verify(workflowService).startFlow(Mockito.anyString(), Mockito.anyMap());
    }

    @Test(expected = TaskboxException.class)
    public void testGetEmptyWorkflowMessage_shouldThrowTaskboxException() throws TaskboxException {
        service.getWorkflowMessage();
    }

    @Test
    public void testWorkflowMessage_shouldSetString() throws TaskboxException {
        service.setWorkflowMessage("testmessage");
        assertEquals("testmessage", service.getWorkflowMessage());
    }

    @Test
    public void testCreateNewTask_shouldReturnNewTask() throws PersistenceException {
        Task newTask = null;
        newTask = internalService.createNewTask("testId", "testContext", "testUser");
        assertEquals("testUser", newTask.getUser());
    }

    @Test
    public void testGetOpenTasks_shouldReturnOpenTasks() {
        List<Task> result = new ArrayList<Task>();
        result.add(new Task());
        when(persistenceService.query(any(Task.class))).thenReturn(result);

        List<Task> ret = service.getOpenTasks();
        assertEquals(1, ret.size());
        for (Task task : result) {
            assertFalse(task.isFinished());
        }
    }
}
