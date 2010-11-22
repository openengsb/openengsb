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

import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.ui.web.model.User;

public class LoginPage extends BasePage {
    private User user = new User();

    @SuppressWarnings("serial")
    public LoginPage() {
        Form<User> loginForm = new Form<User>("form") {
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
        loginForm.setModel(new CompoundPropertyModel<User>(user));
        add(loginForm);
        loginForm.add(new RequiredTextField<String>("username"));
        loginForm.add(new PasswordTextField("password"));
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }
}
