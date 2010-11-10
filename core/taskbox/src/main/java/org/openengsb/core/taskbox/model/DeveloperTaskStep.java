package org.openengsb.core.taskbox.model;

import java.io.Serializable;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.taskbox.web.DeveloperTaskStepPanel;

public class DeveloperTaskStep implements TaskStep, Serializable {
	
	
	
	//name of this step
	private String name;
	
	//description of this step
	private String description;
	
	//Specific DeveloperTaskStep properties:
	// attended working hours, comments of the developer
	private Integer workingHours;
	private String developerComment;
	
	//flag, if step is done or not
	private boolean doneFlag;
	
	@Override
	public boolean getDoneFlag(){
		return this.doneFlag;
	}
	
	@Override
	public void setDoneFlag(boolean doneFlag){
		this.doneFlag = doneFlag;
	}
	
	public DeveloperTaskStep(String name, String description) {
		this.name = name;
		this.description=description;
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

	public void setWorkingHours(Integer workingHours) {
		this.workingHours = workingHours;
	}

	public Integer getWorkingHours() {
		return workingHours;
	}

	public void setDeveloperComment(String developerComment) {
		this.developerComment = developerComment;
	}

	public String getDeveloperComment() {
		return developerComment;
	}

    @Override
    public Panel getPanel(String id) {
        return new DeveloperTaskStepPanel(id, this);
    }

	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();	
}
