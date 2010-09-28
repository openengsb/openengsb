package org.openengsb.core.common.wicket.inject.demopage;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.openengsb.core.common.wicket.inject.OsgiSpringBean;


public class Page extends WebPage {

    @OsgiSpringBean(springBeanName = "testBean", bundleSymbolicName = "testName")
    private PageService testService;

    public Page() {
        add(new Label("testId", testService.getHelloWorldText()));
    }

}
