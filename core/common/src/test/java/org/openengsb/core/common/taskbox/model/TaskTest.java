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

package org.openengsb.core.common.taskbox.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class TaskTest {
    private Task task;

    @Before
    public void init() throws Exception {
        task = new Task();
    }

    @Test
    public void init_shouldInitializeProperties() throws Exception {
        assertTrue(task.getTaskId().length() > 0);
        assertTrue(task.getTaskCreationTimestamp().before(new Date(System.currentTimeMillis() + 10)));
    }
    
    @Test
    public void generateTaskId_shouldSetNewId() throws Exception {
        String id = task.getTaskId();
        assertFalse(task.generateTaskId().equals(id));
        assertFalse(task.getTaskId().equals(id));
    }
}
