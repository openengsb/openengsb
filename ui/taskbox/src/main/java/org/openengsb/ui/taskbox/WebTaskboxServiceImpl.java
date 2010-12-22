package org.openengsb.ui.taskbox;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.WebTaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.model.ProcessBag;
import org.openengsb.core.taskbox.TaskboxServiceImpl;
import org.openengsb.ui.taskbox.model.WebTask;
import org.openengsb.ui.taskbox.web.TaskPanel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WebTaskboxServiceImpl extends TaskboxServiceImpl implements WebTaskboxService {

    private Map<String, String> panelMap = new HashMap<String, String>();
    
    @Override
    public Panel getOverviewPanel() {
        TaskDataProvider provider = new TaskDataProvider();
        provider.setTaskboxService(this);
        TaskOverviewPanel op= new TaskOverviewPanel("Overview",provider);
        return op;
    }
    
    @Override
    public Panel getTaskPanel(String panelId, String taskId){
        ProcessBag bag = new ProcessBag();
        if(panelMap.containsKey(taskId)){
            System.out.println("TaskPanel from Map");
            Panel p = null;
            try {
                Class panelClass = Class.forName(panelMap.get(taskId));
                Constructor panelConstructor = panelClass.getConstructor(String.class, Task.class);
                p = (Panel) panelConstructor.newInstance(panelId, new WebTask(bag));
                
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return p;
        }else{
            System.out.println("Default TaskPanel");
            panelMap.put(taskId, TaskPanel.class.getCanonicalName());
            return new TaskPanel(panelId, new WebTask(bag));
        }
        
    }

    @Override
    public void registerTaskPanel(String taskType, String panelClass) {
        panelMap.put(taskType, panelClass);
    }

}
