package org.openengsb.core.common.taskbox;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.model.Task;

/**
 * The WebTaskboxService extends the normal TaskboxService with a function to generate a standard overview panel.
 */
public interface WebTaskboxService extends TaskboxService {
    
    /**
     * Generates a standard Wicket-Panel, which displays tasks out of the persistence.
     */
    public Panel getOverviewPanel();
    
    /**
     * Gets the Wicket Panel for a Specific Task if it is registered. If Panel is not registered, returns the Default-TaskPanel
     */
    public Panel getTaskPanel(Task task, String wicketPanelId) throws TaskboxException;
    
    /**
     * Register a Specific Panel for a predefined Tasktype
     */
    public void registerTaskPanel(String taskType, Class panelClass);
}
