package org.openengsb.core.taskbox.model;

public interface TaskStep {
	String getName();
	
	String getDescription();
	
	void setDoneFlag(boolean doneFlag);
	
	boolean getDoneFlag();
	
	String getTaskStepType();
	
	String getTaskStepTypeText();
	
	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();
}
