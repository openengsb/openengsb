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

import org.apache.wicket.markup.html.panel.Panel;

public class CompleteTicketInformationStep implements TaskStep {
    // used if a Ticket is not configured sufficient
    // e.g. Type is not chosen, Issue-Info is incomplete

    // name of this step
    private String name;

    // description of this step
    private String description;

    /*
     * Specific CompleteTicketInformationStep properties: missingInformation
     */
    private String missingInformation;

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

    public CompleteTicketInformationStep(String name, String description) {
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

    @Override
	public Panel getPanel(String id) {
	    // TODO Auto-generated method stub
	    return new Panel("null");
    }

    @Override
    public TaskStepType getTaskStepType() {
        return TaskStepType.CompleteTicketInformationStep;
    }

    @Override
    public String getTaskStepTypeText() {
        // String className=this.getClass().getName();
        return "CompleteTicketInformationStep";
    }

    @Override
    public String getTaskStepTypeDescription() {
        return "Complete Ticket Information";
    }

    public void setMissingInformation(String missingInformation) {
        this.missingInformation = missingInformation;
    }

    public String getMissingInformation() {
        return missingInformation;
    }

    // return ID of the According UI Panel
    // WicketPanel createEditingPanel();

    // return ID of the According UI Panel
    // WicketPanel createViewingPanel();
}
