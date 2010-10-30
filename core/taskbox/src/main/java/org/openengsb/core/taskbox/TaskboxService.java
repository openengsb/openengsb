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

public interface TaskboxService {
    /**
     * Gets the message set by a workflow
     * 
     * @return workflow message
     * @throws TaskboxException
     */
    String getWorkflowMessage() throws TaskboxException;

    /**
     * Starts a test workflow
     * 
     * @throws TaskboxException
     */
    void startWorkflow() throws TaskboxException;

    /**
     * Used by a workflow to set a message
     * 
     * @param message to be set
     */
    void setWorkflowMessage(String message);
}
