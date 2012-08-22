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

package org.openengsb.core.workflow.api.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class TaskTest {
    private Task task;
    private Task newtask;

    @Before
    public void init() throws Exception {
        task = new Task();
        newtask = Task.createTaskWithAllValuesSetToNull();
    }

    @Test
    public void testInitializeProperties_shouldWork() throws Exception {
        assertTrue(task.getTaskId().length() > 0);
        assertTrue(task.getTaskCreationTimestamp().before(new Date(System.currentTimeMillis() + 10)));
    }

    @Test
    public void testCreateTaskWithGivenOtherTask_shouldWork() throws Exception {
        newtask.setTaskId("ID_newtask");
        newtask.setDescription("Desc");
        assertFalse(newtask.containsProperty("taskCreationTimestamp"));
        task = new Task(newtask);
        assertTrue(task.getTaskId().equals(newtask.getTaskId()));
        assertTrue(task.getDescription().equals(newtask.getDescription()));
        assertTrue(task.getTaskCreationTimestamp().before(new Date(System.currentTimeMillis() + 10)));
    }
}
