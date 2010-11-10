package org.openengsb.core.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;

public class InformationTaskStep implements TaskStep {
	
	
	//name of this step
	private String name;
	
	//description of this step
	private String description;
	
	//Specific DeveloperTaskStep properties:
	// information message
	private String information;
	
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
	
	public InformationTaskStep(String name, String description) {
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

	public void setInformation(String information) {
		this.information = information;
	}

	public String getInformation() {
		return information;
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
