package org.openengsb.core.common.taskbox;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * The WebTaskboxService extends the normal TaskboxService with a function to generate a standard overview panel.
 */
public interface WebTaskboxService extends TaskboxService {
    
    /**
     * Generates a standard Wicket-Panel, which displays tasks out of the persistence.
     */
    public Panel getOverviewPanel();
}
