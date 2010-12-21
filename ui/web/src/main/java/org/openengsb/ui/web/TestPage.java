package org.openengsb.ui.web;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.taskbox.WebTaskboxService;

public class TestPage extends BasePage{
    
    @SpringBean
    private WebTaskboxService taskboxService;
    public TestPage(){
        System.out.println("lets begin OVERVIEWPANEL!");
        Panel panel= taskboxService.getTaskPanel("panel", "test");
        System.out.println("I RETRIEVED A PANEL WHO WOULD BELIEVE IT!!");
        this.add(panel);
        System.out.println("lets end OVERVIEWPANEL!");
    }
}
