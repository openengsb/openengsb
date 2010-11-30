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

import org.openengsb.core.common.Event;

/**
 * This event is used to finish taskbox sub-workflows and to indicate that a
 * human task has finished
 */
public class TaskFinishedEvent extends Event {

    /**
     * Each TaskFinishedEvent has a ProcessBag
     */
    private ProcessBag processBag;

    /**
     * Constructor
     * 
     * @param processBag
     */
    public TaskFinishedEvent(ProcessBag processBag) {
        this.processBag = processBag;
    }

    /**
     * for setting the Event's ProcessBag
     * 
     * @param processBag
     */
    public void setProcessBag(ProcessBag processBag) {
        this.processBag = processBag;
    }

    /**
     * get the Event's processBag
     * 
     * @return processBag
     */
    public ProcessBag getProcessBag() {
        return processBag;
    }
}
