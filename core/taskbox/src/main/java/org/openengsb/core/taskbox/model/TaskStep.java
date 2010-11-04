package org.openengsb.core.taskbox.model;

public interface TaskStep {
	String getName();
	
	String getDescription();
	
	//return ID of the According UI Panel
	String getUIId();
}
