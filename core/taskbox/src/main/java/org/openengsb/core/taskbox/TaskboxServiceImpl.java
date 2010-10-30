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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.WorkflowService;

public class TaskboxServiceImpl implements TaskboxService {
    private Log log = LogFactory.getLog(getClass());

    private WorkflowService workflowService;

    private String message;

    public void init() {

    }

    @Override
    public String getWorkflowMessage() throws TaskboxException {
        if (message == null) {
            throw new TaskboxException();
        }

        return message;
    }

    @Override
    public void setWorkflowMessage(String message) {
        this.message = message;
    }

    @Override
    public void startWorkflow() throws TaskboxException {
        try {
            workflowService.startFlow("tasktest");
            log.trace("Started workflow 'tasktest'");
        } catch (WorkflowException e) {
            throw new TaskboxException(e);
        }
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
}
