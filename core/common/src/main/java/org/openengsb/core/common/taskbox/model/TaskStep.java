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

/**
 * A TaskStep represents one action that has to be done in a Task
 * (e.g.: Task = handling of a Ticket in a SupportSystem
 * TaskStep(s) = Filling out information about the Request;
 *               Processing a Change Request;
 *               Reviewing the Change Request;
 *               Sending Deliverables to Customer;
 *               Closing Ticket;)
 */
public interface TaskStep {
    /**
     * returns the Name of the TaskStep
     */
    String getName();

    /**
     * returns a Description for the TaskStep
     */
    String getDescription();

    /**
     * sets the DoneFlag - used to mark a TaskStep as completed
     */
    void setDoneFlag(boolean doneFlag);

    /**
     * returns the Completion State of a TaskStep
     */
    boolean getDoneFlag();
    
    /**
     * returns a Description according to the Type of a TaskStep
     */
    String getTaskStepTypeDescription();
    
    /**
     * returns the Type of a TaskStep
     */
    String getTaskStepType();
}
