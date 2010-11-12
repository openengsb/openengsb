/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.ui.web.global.footer.FooterTemplate;
import org.openengsb.ui.web.global.header.HeaderTemplate;

public class BasePage extends WebPage {
    @SpringBean
    private ContextCurrentService contextService;

    public BasePage() {
        add(new HeaderTemplate("header", getHeaderMenuItem(), this));
        add(new FooterTemplate("footer"));
        initContextForCurrentThread();
    }

    /**
     * @return the class name, which should be the index in navigation bar
     */
    public String getHeaderMenuItem() {
        return this.getClass().getSimpleName();
    }

    final void initContextForCurrentThread() {
        String sessionContextId = getSessionContextId();
        try {
            if (contextService != null) {
                contextService.setThreadLocalContext(sessionContextId);
            }
        } catch (IllegalArgumentException e) {
            contextService.createContext(sessionContextId);
            contextService.createContext(sessionContextId + "2");
            contextService.setThreadLocalContext(sessionContextId);
            contextService.putValue("domain/NotificationDomain/defaultConnector/id", "notification");
            contextService.putValue("domain/IssueDomain/defaultConnector/id", "issue");
            contextService.putValue("domain/ExampleDomain/defaultConnector/id", "example");
        }
    }

    public String getSessionContextId() {
        WicketSession session = WicketSession.get();
        if (session == null) {
            return "foo";
        }
        if (session.getThreadContextId() == null) {
            setThreadLocalContext("foo");
        }
        return session.getThreadContextId();
    }

    public void setThreadLocalContext(String threadLocalContext) {
        WicketSession session = WicketSession.get();
        if (session != null) {
            session.setThreadContextId(threadLocalContext);
        }
    }

    public List<String> getAvailableContexts() {
        if (contextService == null) {
            return new ArrayList<String>();
        }
        return contextService.getAvailableContexts();
    }
}
