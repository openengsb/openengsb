package org.openengsb.core.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;

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

    @Override
    public Panel getPanel(String id) {
        // TODO Auto-generated method stub
        return new Panel("null");
    }

	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();
}
