package org.openengsb.core.taskbox.model;

public interface TaskStep {
	String getName();
	
	String getDescription();
	
	void setDoneFlag(boolean doneFlag);
	
	boolean getDoneFlag();
	
	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	//return ID of the According UI Panel
	//WicketPanel createViewingPanel();
}
