package org.openengsb.core.taskbox.model;

public interface Task {
	
	String getId();
	
	String getType();
	
	void setType(String type);
	
	//TaskStep getCurrentTaskStep();
	//void setCurrentTaskStep();
	
	//return ID of the UI Panel to display the Tasks Details
	//String getUIDetailId();
}
