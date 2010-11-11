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

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.openengsb.ui.common.wicket.OpenEngSBPage;
import org.openengsb.ui.web.global.footer.FooterTemplate;
import org.openengsb.ui.web.global.header.HeaderTemplate;

@SuppressWarnings("serial")
@AuthorizeInstantiation("ROLE_USER")
public class BasePage extends OpenEngSBPage {

    public BasePage() {
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
        Form<?> form = new Form<Object>("projectChoiceForm");
        form.add(createProjectChoice());
        add(form);
        form.add(new Link<Object>("logout") {
            @Override
            public void onClick() {
                ((AuthenticatedWebSession) this.getSession()).signOut();
                setResponsePage(LoginPage.class);
            }
        });

        this.add(new HeaderTemplate("header", this.getHeaderMenuItem()));
        this.add(new FooterTemplate("footer"));
    }

    private Component createProjectChoice() {
        DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("projectChoice", new IModel<String>() {
            public String getObject() {
                return getSessionContextId();
            }

            public void setObject(String object) {
                setThreadLocalContext(object);
            }

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
}
