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

package org.openengsb.core.common.workflow.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowEditorServiceImpl implements WorkflowEditorService {

    private final Map<String, Workflow> workflows = new HashMap<String, Workflow>();

    private Workflow currentWorkflow;

    @Override
    public List<String> getWorkflowNames() {
        ArrayList<String> arrayList = new ArrayList<String>(this.workflows.keySet());
        Collections.sort(arrayList);
        return arrayList;
    }

    @Override
    public Workflow loadWorkflow(String name) {
        if (workflows.containsKey(name)) {
            currentWorkflow = workflows.get(name);
            return currentWorkflow;
        } else {
            throw new IllegalArgumentException("Workflow Name doesn't exist");
        }
    }

    @Override
    public Workflow getCurrentWorkflow() {
        return currentWorkflow;
    }

    @Override
    public void saveCurrentWorkflow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void createWorkflow(String name) {
        if (workflows.containsKey(name)) {
            throw new IllegalArgumentException("Workflow with same name already exists");
        } else {
            Workflow workflow = new Workflow();
            workflow.setName(name);
            workflows.put(name, workflow);
            currentWorkflow = workflow;
        }
    }
}
