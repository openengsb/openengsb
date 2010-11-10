package org.openengsb.core.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;

public class ReviewerTaskStep implements TaskStep {
	
	
	//name of this step
	private String name;
	
	//description of this step
	private String description;
	
	//Specific DeveloperTaskStep properties:
	// reviewStatus (OK=true, NOK=false), feedback message
	private boolean reviewStatus;
	private String feedback;
	
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
	
	public ReviewerTaskStep(String name, String description) {
		this.name = name;
		this.description=description;
		this.doneFlag = false;
	}
	
	
	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setReviewStatus(boolean reviewStatus) {
		this.reviewStatus = reviewStatus;
	}

	public boolean getReviewStatus() {
		return reviewStatus;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public String getFeedback() {
		return feedback;
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
