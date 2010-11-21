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
 * A Task is handled by the TaskboxService and consists of 1 or more TaskSteps.
 * It represents a more or less long running process (e.g.: handling of a Ticket
 * in a SupportSystem) 
 */
public interface Task {
    /**
     * returns the Unique ID the Task can be identified with
     */
    String getId();

    /**
     * sets the Unique ID the Task can be identified with
     */
    void setId(String id);

    /**
     * returns the Type of the Task. 
     * The Type is used to group similar Tasks together
     */
    String getType();

    /**
     * sets the Type of the Task
     */
    void setType(String type);
}
