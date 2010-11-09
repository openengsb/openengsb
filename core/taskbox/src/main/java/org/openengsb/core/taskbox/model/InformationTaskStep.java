/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.openengsb.core.taskbox.model;

public class InformationTaskStep implements TaskStep {

    // name of this step
    private String name;

    // description of this step
    private String description;

    /*
     * Specific DeveloperTaskStep properties: information message
     */
    private String information;

    // flag, if step is done or not
    private boolean doneFlag;

    @Override
    public boolean getDoneFlag() {
        return this.doneFlag;
    }

    @Override
    public void setDoneFlag(boolean doneFlag) {
        this.doneFlag = doneFlag;
    }

    public InformationTaskStep(String name, String description) {
        this.name = name;
        this.description = description;
        this.doneFlag = false;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getInformation() {
        return information;
    }

    @Override
    public String getTaskStepTypeText() {
        // String className=this.getClass().getName();
        return "InformationTaskStep";
    }

    @Override
    public String getTaskStepTypeDescription() {
        return "Information";
    }

    @Override
    public TaskStepType getTaskStepType() {
        return TaskStepType.InformationTaskStep;
    }

    // return ID of the According UI Panel
    // WicketPanel createEditingPanel();

    // return ID of the According UI Panel
    // WicketPanel createViewingPanel();
}
