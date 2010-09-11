/**

 Copyright 2010 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.ui.web.global.footer.FooterTemplate;
import org.openengsb.ui.web.global.header.HeaderTemplate;
import org.openengsb.ui.web.model.CurrentContextModel;

@SuppressWarnings("serial")
public class BasePage extends WebPage {

    @SpringBean
    private ContextCurrentService contextService;

    public BasePage() {
        initDummyContext();
        initWebPage();
    }

    private void initWebPage() {
        add(new Link<Object>("lang.en") {
            @Override
            public void onClick() {
                this.getSession().setLocale(Locale.ENGLISH);
            }
        });
        add(new Link<Object>("lang.de") {
            @Override
            public void onClick() {
                this.getSession().setLocale(Locale.GERMAN);
            }
        });

        add(createProjectChoice());

        this.add(new HeaderTemplate("header", this.getHeaderMenuItem()));
        this.add(new FooterTemplate("footer"));
    }

    private Component createProjectChoice() {
        return new DropDownChoice<String>("projectChoice", new CurrentContextModel(contextService),
                getAvailableContexts());
    }

    private List<String> getAvailableContexts() {
        if (contextService == null) {
            return new ArrayList<String>();
        }
        return contextService.getAvailableContexts();
    }

    final void initDummyContext() {
        try {
            if (contextService != null) {
                contextService.setThreadLocalContext("foo");
            }
        } catch (IllegalArgumentException e) {
            contextService.createContext("foo");
            contextService.createContext("bar");
            contextService.setThreadLocalContext("foo");
            contextService.putValue("domains/notification/defaultConnector/id", "notification");
            contextService.putValue("domains/example/defaultConnector/id", "example");

        }
    }

    /**
     * @return the class name, which should be the index in navigation bar
     * 
     */
    public final String getHeaderMenuItem() {
        return this.getClass().getSimpleName();
    }
}
