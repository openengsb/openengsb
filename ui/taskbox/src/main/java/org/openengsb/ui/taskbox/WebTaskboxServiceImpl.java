package org.openengsb.ui.taskbox;


import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.WebTaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.model.ProcessBag;
import org.openengsb.core.taskbox.TaskboxServiceImpl;
import org.openengsb.ui.taskbox.model.WebTask;
import org.openengsb.ui.taskbox.web.TaskPanel;

public class WebTaskboxServiceImpl extends TaskboxServiceImpl implements WebTaskboxService {

    @Override
    public Panel getOverviewPanel() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Panel getTaskPanel(String panelId, String taskId){
        ProcessBag bag = new ProcessBag();
        return new TaskPanel(panelId, new WebTask(bag));
    }

}
