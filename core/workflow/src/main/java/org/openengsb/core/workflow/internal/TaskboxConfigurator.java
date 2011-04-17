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

package org.openengsb.core.workflow.internal;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.TaskboxServiceInternal;
import org.openengsb.core.api.workflow.model.InternalWorkflowEvent;
import org.openengsb.core.api.workflow.model.ProcessBag;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds everything needed for humann interaction into the rulemanager, (humantask workflow, taskbox global etc.).
 */
public class TaskboxConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskboxConfigurator.class);

    private RuleManager ruleManager;

    public void init() {
        if (!ruleManager.listGlobals().containsKey("taskbox")) {
            addGlobalsAndImports();
            addWorkflow();
        }
    }

    private void addGlobalsAndImports() {
        try {
            ruleManager.addGlobal(TaskboxService.class.getCanonicalName(), "taskbox");
            ruleManager.addGlobal(TaskboxServiceInternal.class.getCanonicalName(), "taskboxinternal");
            ruleManager.addImport(InternalWorkflowEvent.class.getCanonicalName());
            ruleManager.addImport(ProcessBag.class.getCanonicalName());
        } catch (RuleBaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addWorkflow() {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/humantask.rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, "humantask");
            ruleManager.add(id, testWorkflow);

            LOGGER.info("loaded workflow 'humanTask'");
        } catch (RuleBaseException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }
}
