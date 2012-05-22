/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.admin.basePage;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.ui.admin.global.footer.footerTemplate.FooterTemplate;
import org.openengsb.ui.admin.global.header.HeaderTemplate;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.loginPage.LoginPage;
import org.openengsb.ui.common.OpenEngSBPage;
import org.openengsb.ui.common.OpenEngSBWebSession;

@SuppressWarnings("serial")
public abstract class BasePage extends OpenEngSBPage {

    public BasePage() {
        initCommonContent();
    }

    private void initCommonContent() {
        initializeHeader();
        initializeLoginLogoutTemplate();
        initializeFooter();
    }

    public BasePage(PageParameters parameters) {
        super(parameters);
        initCommonContent();
    }

    private void initializeFooter() {
        add(new FooterTemplate("footer"));
    }

    private void initializeLoginLogoutTemplate() {
        Form<?> form = new Form<Object>("projectChoiceForm");
        form.add(createProjectChoice());
        add(form);
        try {
            form.setVisible(((OpenEngSBWebSession) WebSession.get()).isSignedIn());
        } catch (ClassCastException e) {
        }

        Link<Object> link = new Link<Object>("logout") {
            @Override
            public void onClick() {
                boolean signedIn = ((OpenEngSBWebSession) WebSession.get()).isSignedIn();
                if (signedIn) {
                    ((AuthenticatedWebSession) getSession()).signOut();
                }
                setResponsePage(signedIn ? Index.class : LoginPage.class);
            }
        };
        add(link);

        WebMarkupContainer container = new WebMarkupContainer("logintext");
        link.add(container);
        try {
            container.setVisible(!((OpenEngSBWebSession) WebSession.get()).isSignedIn());
        } catch (ClassCastException e) {
        }
        container = new WebMarkupContainer("logouttext");
        link.add(container);
        try {
            container.setVisible(((OpenEngSBWebSession) WebSession.get()).isSignedIn());
        } catch (ClassCastException e) {
        }
    }

    private void initializeHeader() {
        add(new HeaderTemplate("header", getHeaderMenuItem()));
    }

    private Component createProjectChoice() {
        DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("projectChoice", new IModel<String>() {
            @Override
            public String getObject() {
                return getSessionContextId();
            }

            @Override
            public void setObject(String object) {
                ContextHolder.get().setCurrentContextId(object);
            }

            @Override
            public void detach() {
            }
        }, getAvailableContexts()) {
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            @Override
            protected void onModelChanged() {
                setResponsePage(BasePage.this.getClass());
            }

        };
        return dropDownChoice;
    }

    /**
     * @return the class name, which should be the index in navigation bar
     *
     */
    @Override
    public String getHeaderMenuItem() {
        return this.getClass().getSimpleName();
    }

    public String getSessionContextId() {
        OpenEngSBWebSession session = OpenEngSBWebSession.get();
        if (session == null) {
            return "foo";
        }
        String contextId = ContextHolder.get().getCurrentContextId();
        if (contextId == null) {
            ContextHolder.get().setCurrentContextId("foo");
            return contextId;
        }
        return contextId;
    }
}
