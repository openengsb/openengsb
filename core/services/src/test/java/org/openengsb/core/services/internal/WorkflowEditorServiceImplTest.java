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

package org.openengsb.core.services.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;

public class WorkflowEditorServiceImplTest {

    private WorkflowEditorServiceImpl service;

    @Before
    public void setUp() throws InvalidConfigurationException, PersistenceException {
        service = new WorkflowEditorServiceImpl();
    }

    @Test
    public void createWorkflow_shouldBeSetAsCurrentWorkflow() {
        String name = "name";
        service.createWorkflow(name);
        WorkflowRepresentation currentWorkflow = service.getCurrentWorkflow();
        assertThat(name, equalTo(currentWorkflow.getName()));
    }

    @Test
    public void loadWorkflow_shouldBeSetAsCurrentWorkflow() {
        String name = "name";
        service.createWorkflow(name);
        String string = "123";
        service.createWorkflow(string);
        assertThat(string, equalTo(service.getCurrentWorkflow().getName()));
        
        service.loadWorkflow(name);
        
        assertThat(name, equalTo(service.getCurrentWorkflow().getName()));
    }

    @Test
    public void getWorkflowName_shouldBeSetAsCurrentWorkflow() {
        String name = "name";
        service.createWorkflow(name);
        String string = "123";
        
        service.createWorkflow(string);
        
        assertThat(string, equalTo(service.getWorkflowNames().get(0)));
        assertThat(name, equalTo(service.getWorkflowNames().get(1)));
    }

    @Test
    public void callCurrentWorkflow_shouldReturnNullWhenNoWorkflowSelected() {
        assertThat(null, equalTo(service.getCurrentWorkflow()));
    }
}
