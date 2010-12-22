package org.openengsb.ui.web;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.taskbox.WebTaskboxService;

public class TaskPage extends BasePage{
    
    @SpringBean
    private WebTaskboxService taskboxService;
    public TaskPage(PageParameters parameters){
        System.out.println("lets begin OVERVIEWPANEL!");
        Panel panel= taskboxService.getTaskPanel("panel", "TestType");
        System.out.println("I RETRIEVED A PANEL WHO WOULD BELIEVE IT!!");
        this.add(panel);
        System.out.println("lets end OVERVIEWPANEL!");
    }
}
