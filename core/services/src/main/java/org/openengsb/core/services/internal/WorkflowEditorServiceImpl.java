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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;

public class WorkflowEditorServiceImpl implements WorkflowEditorService {

    private final Map<String, WorkflowRepresentation> workflows = new HashMap<String, WorkflowRepresentation>();

    private WorkflowRepresentation currentWorkflow;

    @Override
    public List<String> getWorkflowNames() {
        ArrayList<String> arrayList = new ArrayList<String>(workflows.keySet());
        Collections.sort(arrayList);
        return arrayList;
    }

    @Override
    public WorkflowRepresentation loadWorkflow(String name) {
        if (workflows.containsKey(name)) {
            currentWorkflow = workflows.get(name);
            return currentWorkflow;
        } else {
            throw new IllegalArgumentException("Workflow Name doesn't exist");
        }
    }

    @Override
    public WorkflowRepresentation getCurrentWorkflow() {
        return currentWorkflow;
    }

    @Override
    public void saveCurrentWorkflow() {

    }

    @Override
    public void createWorkflow(String name) {
        if (workflows.containsKey(name)) {
            throw new IllegalArgumentException("Workflow with same name already exists");
        } else {
            WorkflowRepresentation workflow = new WorkflowRepresentation();
            workflow.setName(name);
            workflows.put(name, workflow);
            currentWorkflow = workflow;
        }
    }
}
