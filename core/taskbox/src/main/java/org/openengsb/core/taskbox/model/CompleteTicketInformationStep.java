package org.openengsb.core.taskbox.model;

public class CompleteTicketInformationStep implements TaskStep {
	//used if a Ticket is not configured sufficient
	//e.g. Type is not chosen, Issue-Info is incomplete
	
	//name of this step
	private String name;
	
	//description of this step
	private String description;
	
	//Specific CompleteTicketInformationStep properties:
	// missingInformation
	private String missingInformation;
	
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
	
	public CompleteTicketInformationStep(String name, String description) {
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

	@Override
	public String getTaskStepType() {
		//String className=this.getClass().getName();
		return "CompleteTicketInformationStep";
	}

	@Override
	public String getTaskStepTypeText() {
		return "Complete Ticket Information";
	}

	public void setMissingInformation(String missingInformation) {
		this.missingInformation = missingInformation;
	}

	public String getMissingInformation() {
		return missingInformation;
	}

	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();
}
