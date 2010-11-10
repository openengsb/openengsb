package org.openengsb.core.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;

public interface TaskStep {
	String getName();
	
	String getDescription();
	
	void setDoneFlag(boolean doneFlag);
	
	boolean getDoneFlag();
	
	//return ID of the According UI Panel
	//WicketPanel createEditingPanel();
	
	/**
     * returns the Wicket Panel for this task
     */
    Panel getPanel(String id);
}
