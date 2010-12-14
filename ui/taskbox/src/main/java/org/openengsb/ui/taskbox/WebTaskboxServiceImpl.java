package org.openengsb.ui.taskbox;


import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.WebTaskboxService;
import org.openengsb.core.taskbox.TaskboxServiceImpl;

public class WebTaskboxServiceImpl extends TaskboxServiceImpl implements WebTaskboxService {

    @Override
    public Panel getOverviewPanel() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Panel getTaskPanel(String panelId, String taskId){
        return new TaskPanel(panelId, getTaskForId(taskId));
    }

}
