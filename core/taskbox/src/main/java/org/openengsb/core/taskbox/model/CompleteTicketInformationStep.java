package org.openengsb.core.taskbox.model;

public class CompleteTicketInformationStep implements TaskStep {
	//used if a Ticket is not configured sufficient
	//e.g. Type is not chosen, Issue-Info is incomplete
	
	//name of this step
	private String name;
	
	//description of this step
	private String description;
	
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

	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();
}
