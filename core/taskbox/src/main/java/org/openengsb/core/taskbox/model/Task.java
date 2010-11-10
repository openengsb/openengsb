package org.openengsb.core.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;

public interface Task {
	
	String getId();
	
	String getType();
	
	void setType(String type);
	
	//TaskStep getCurrentTaskStep();
	//void setCurrentTaskStep(TaskStep taskStep);
	
	/**
	 * returns the Wicket Panel for this task
	 */
	Panel getPanel(String id);
}
