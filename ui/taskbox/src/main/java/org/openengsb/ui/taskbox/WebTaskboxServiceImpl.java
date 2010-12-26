package org.openengsb.ui.taskbox;


import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.WebTaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.taskbox.TaskboxServiceImpl;
import org.openengsb.ui.taskbox.web.TaskPanel;

public class WebTaskboxServiceImpl extends TaskboxServiceImpl implements WebTaskboxService {

    private Map<String, Class> panelMap = new HashMap<String, Class>();
    
    @Override
    public Panel getOverviewPanel() {
        return null;
    }
    
    @Override
    public Panel getTaskPanel(Task task, String wicketPanelId) throws TaskboxException {
        if(panelMap.containsKey(task.getTaskType())){
            Panel p = null;
            try {
                Class panelClass = panelMap.get(task.getTaskType());
                Constructor panelConstructor = panelClass.getConstructor(String.class, Task.class);
                p = (Panel) panelConstructor.newInstance(wicketPanelId, task);
            } catch (Exception e) {
                throw new TaskboxException(e);
            }
            return p;
        } else {
            registerTaskPanel(task.getTaskType(),TaskPanel.class);
            return getTaskPanel(task, wicketPanelId);
        }
    }

    @Override
    public void registerTaskPanel(String taskType, Class panelClass) {
        panelMap.put(taskType, panelClass);
    }

}
