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

package org.openengsb.ui.admin.loginPage;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.model.UsernamePassword;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@PaxWicketMountPoint(mountPoint = "login")
public class LoginPage extends BasePage {

    private static final long serialVersionUID = 4704550987311760491L;

    private UsernamePassword user = new UsernamePassword();

    public LoginPage() {
        initContent();
    }

    private void initContent() {
        @SuppressWarnings("serial")
        Form<UsernamePassword> loginForm = new Form<UsernamePassword>("loginForm") {
            @Override
            protected void onSubmit() {
                AuthenticatedWebSession session = AuthenticatedWebSession.get();
                if (session.signIn(user.getUsername(), user.getPassword())) {
                    setDefaultResponsePageIfNecessary();
                } else {
                    error(new StringResourceModel("error", this, null).getString());
                }
            }

            private void setDefaultResponsePageIfNecessary() {
                if (!continueToOriginalDestination()) {
                    setResponsePage(getApplication().getHomePage());
                }
            }
        };
        loginForm.setModel(new CompoundPropertyModel<UsernamePassword>(user));
        add(loginForm);
        loginForm.add(new RequiredTextField<String>("username"));
        loginForm.add(new PasswordTextField("password"));
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    public LoginPage(PageParameters parameters) {
        super(parameters);
    }

}
