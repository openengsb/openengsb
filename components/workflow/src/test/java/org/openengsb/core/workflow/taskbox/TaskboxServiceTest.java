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

package org.openengsb.core.workflow.taskbox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.TaskboxException;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.InternalWorkflowEvent;
import org.openengsb.core.api.workflow.model.ProcessBag;
import org.openengsb.core.api.workflow.model.Task;
import org.openengsb.core.workflow.internal.TaskboxServiceImpl;
import org.openengsb.core.workflow.internal.TaskboxServiceInternalImpl;
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

    @Test
    public void testCreateNewTask_shouldReturnNewTask() throws PersistenceException {
        internalService.createNewTask(new ProcessBag());
        verify(persistenceService).create(any(Task.class));
    }

    @Test
    public void testGetOpenTasks_shouldReturnOpenTasks() {
        List<Task> result = new ArrayList<Task>();
        result.add(new Task());
        when(persistenceService.query(any(Task.class))).thenReturn(result);

        List<Task> ret = service.getOpenTasks();
        assertEquals(1, ret.size());
    }

    @Test
    public void testGetTaskForId_shouldRunQuery() {
        Task task = Task.createTaskWithAllValuesSetToNull();
        task.setTaskId("1");

        try {
            service.getTaskForId("1");
        } catch (TaskboxException e) {
        }
        verify(persistenceService).query(any(Task.class));
    }

    @Test
    public void testGetTaskForProcessId_shouldRunQuery() throws TaskboxException {
        Task task = Task.createTaskWithAllValuesSetToNull();
        task.setProcessId("1");

        service.getTasksForProcessId("1");
        verify(persistenceService).query(any(Task.class));
    }

    @Test(expected = TaskboxException.class)
    public void testGetTaskForId_shouldThrowExceptionWhenNothingFound() throws TaskboxException {
        Task task = Task.createTaskWithAllValuesSetToNull();
        task.setTaskId("1");
        when(persistenceService.query(any(Task.class))).thenReturn(new ArrayList<Task>());
        service.getTaskForId("1");
    }

    @Test(expected = TaskboxException.class)
    public void testGetTaskForId_shouldThrowExceptionWhenMoreThanOneFound() throws TaskboxException {
        Task task = Task.createTaskWithAllValuesSetToNull();
        task.setTaskId("1");

        List<Task> list = new ArrayList<Task>();
        list.add(task);
        list.add(task);
        when(persistenceService.query(any(Task.class))).thenReturn(list);
        service.getTaskForId("1");
    }

    @Test
    public void testFinishTask_shouldProcessEvent() throws PersistenceException, WorkflowException {
        Task task = new Task();
        task.setProcessId("1");
        List<Task> result = new ArrayList<Task>();
        result.add(task);
        when(persistenceService.query(any(Task.class))).thenReturn(result);

        service.finishTask(task);
        verify(workflowService).processEvent(any(InternalWorkflowEvent.class));
    }

    @Test
    public void testFinishTaskTwice_shouldProcessEventOnlyOnce() throws PersistenceException, WorkflowException {
        Task task = new Task();
        task.setProcessId("1");
        List<Task> result = new ArrayList<Task>();
        result.add(task);
        when(persistenceService.query(any(Task.class))).thenReturn(result);
        service.finishTask(task);

        result = new ArrayList<Task>();
        when(persistenceService.query(any(Task.class))).thenReturn(result);
        service.finishTask(task);

        verify(workflowService, times(1)).processEvent(any(InternalWorkflowEvent.class));
    }

    @Test
    public void testFinishTask_shouldDeleteAndProcessEvent() throws PersistenceException, WorkflowException {
        Task task = new Task();
        task.setProcessId("1");
        List<Task> result = new ArrayList<Task>();
        result.add(task);
        when(persistenceService.query(any(Task.class))).thenReturn(result);

        ProcessBag bag = new ProcessBag(task);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("processBag", bag);
        workflowService.startFlow("TaskDemoWorkflow", map);
        service.finishTask(task);

        verify(persistenceService).delete(any(Task.class));
        verify(workflowService).processEvent(any(InternalWorkflowEvent.class));
    }

    @Test
    public void testUpdateTask_shouldReturnUpdatedTask() throws PersistenceException {
        Task task = new Task();
        task.setProcessId("1");
        List<Task> result = new ArrayList<Task>();
        result.add(task);
        when(persistenceService.query(any(Task.class))).thenReturn(result);

        Task newTask = Task.createTaskWithAllValuesSetToNull();
        newTask.setTaskId(task.getTaskId());
        newTask.setDescription("test");
        newTask.setProcessId("1");

        service.updateTask(newTask);
        verify(persistenceService).update(task, newTask);
    }
}
