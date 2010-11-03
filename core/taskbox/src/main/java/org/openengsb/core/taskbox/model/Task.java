package org.openengsb.core.taskbox.model;

public interface Task {
	
	String getId();
	
	String getType();
	
	void setType(String type);
	
	//TaskStep getCurrentTaskStep();
}
