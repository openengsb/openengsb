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

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowEditorServiceImpl implements WorkflowEditorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowEditorServiceImpl.class);

    private final Map<String, WorkflowRepresentation> workflows = new HashMap<String, WorkflowRepresentation>();

    private WorkflowRepresentation currentWorkflow;

    private ConfigPersistenceService getPersistence() {
        return OpenEngSBCoreServices
            .getConfigPersistenceService("WORKFLOW");
    }

    @Override
    public void loadWorkflowsFromDatabase() throws PersistenceException {
        try {
            List<ConfigItem<WorkflowRepresentation>> load = getPersistence().load(new HashMap<String, String>());
            for (ConfigItem<WorkflowRepresentation> configItem : load) {
                final WorkflowRepresentation content = configItem.getContent();
                workflows.put(content.getName(), content);
            }
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Invalid Configuration: " + e.getMessage());
            throw new PersistenceException(e);
        }
    }

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
            LOGGER.debug("Loading Workflow: " + name);
            return currentWorkflow;
        } else {
            LOGGER.debug("Loading Workflow " + name + " failed");
            throw new IllegalArgumentException("Workflow Name doesn't exist");
        }
    }

    @Override
    public WorkflowRepresentation getCurrentWorkflow() {
        return currentWorkflow;
    }

    @Override
    public void saveCurrentWorkflow() throws PersistenceException {
        ConfigItem<WorkflowRepresentation> workflowRepresentation = new ConfigItem<WorkflowRepresentation>();
        workflowRepresentation.setContent(getCurrentWorkflow());
        try {
            LOGGER.debug("Saving workflow " + getCurrentWorkflow().getName());
            getPersistence().persist(workflowRepresentation);
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Invalid Configuration: " + e.getMessage());
            throw new PersistenceException(e);
        }
    }

    @Override
    public void createWorkflow(String name) {
        if (workflows.containsKey(name)) {
            LOGGER.info("Creation of Workflow with same name failed");
            throw new IllegalArgumentException("Workflow with same name already exists");
        } else {
            LOGGER.info("Creating Workflow " + name);
            WorkflowRepresentation workflow = new WorkflowRepresentation();
            workflow.setName(name);
            workflows.put(name, workflow);
            currentWorkflow = workflow;
        }
    }
}
