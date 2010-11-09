package org.openengsb.core.taskbox.model;

public class DeveloperTaskStep implements TaskStep {
	
	
	
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
	public String getTaskStepTypeText() {
		//String className=this.getClass().getName();
		return "DeveloperTaskStep";
	}

	@Override
	public String getTaskStepTypeDescription() {
		return "Development";
	}

	@Override
	public TaskStepType getTaskStepType() {
		return TaskStepType.DeveloperTaskStep;
	}

	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();	
}
