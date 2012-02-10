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

package org.openengsb.core.api.workflow.model;

import org.openengsb.core.api.Event;

/**
 * This event is used for internal workflow control actions like the completion of a human task. It contains a workflows
 * ProcessBag.
 */
public interface InternalWorkflowEvent extends Event {
    
    ProcessBag getProcessBag();
    
    void setProcessBag(ProcessBag processBag);
    
    String getType();
    
    void setType(String type);
    
//    private ProcessBag processBag;
//    private String type;
//
//    public InternalWorkflowEvent() {
//        this.processBag = new ProcessBag();
//    }
//
//    public InternalWorkflowEvent(String type) {
//        this.type = type;
//        this.processBag = new ProcessBag();
//    }
//
//    public InternalWorkflowEvent(ProcessBag processBag) {
//        this.processBag = processBag;
//    }
//
//    public InternalWorkflowEvent(String type, ProcessBag processBag) {
//        this.type = type;
//        this.processBag = processBag;
//    }
//
//    @Override
//    public String getType() {
//        if (type != null) {
//            return type;
//        } else {
//            return super.getType();
//        }
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public void setProcessBag(ProcessBag processBag) {
//        this.processBag = processBag;
//    }
//
//    public ProcessBag getProcessBag() {
//        return processBag;
//    }
}
