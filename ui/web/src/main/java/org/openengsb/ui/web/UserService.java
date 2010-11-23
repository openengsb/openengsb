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

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.usermanagement.UserManager;
import org.openengsb.core.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.usermanagement.model.User;

@AuthorizeInstantiation("ROLE_USER")
public class UserService extends BasePage {

    private static Log log = LogFactory.getLog(UserService.class);
    UserInput input = new UserInput();
    RequiredTextField<String> usernameField;
    PasswordTextField passwordField;
    private PasswordTextField passwordVerficationField;

    @SpringBean
    private UserManager userManager;

    public UserService() {
        final WebMarkupContainer usermanagementContainer = new WebMarkupContainer("usermanagementContainer");
        usermanagementContainer.setOutputMarkupId(true);

        final Form<UserInput> userForm = new Form<UserInput>("form") {
            @Override
            protected void onSubmit() {
                createUser();
            }


        };


        usernameField = new RequiredTextField<String>("username");
        passwordField = new PasswordTextField("password");
        passwordVerficationField = new PasswordTextField("passwordVerification");
        usernameField.setOutputMarkupId(true);
        passwordField.setOutputMarkupId(true);
        passwordVerficationField.setOutputMarkupId(true);

        userForm.add(usernameField);
        userForm.add(passwordField);
        userForm.add(passwordVerficationField);

        userForm.setModel(new CompoundPropertyModel<UserInput>(input));

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        userForm.add(feedbackPanel);

        Button resetButton = new Button("resetbutton") {
            @Override
            public void onSubmit() {
                input = new UserInput();
                usernameField.setEnabled(true);
            }
        };

        resetButton.setDefaultFormProcessing(false);
        userForm.add(resetButton);

        IModel<List<User>> userList = new LoadableDetachableModel<List<User>>() {
            @Override
            protected List<User> load() {
                return userManager.getAllUser();
            }
        };
        ListView<User> users = new ListView<User>("users", userList) {
            @Override
            protected void populateItem(final ListItem<User> userListItem) {
                userListItem.add(new Label("user.name", userListItem.getModelObject().getUsername()));
                userListItem.add(new AjaxLink<User>("user.delete") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        userManager.deleteUser(userListItem.getModelObject().getUsername());
                        getList().remove(userListItem.getModelObject());
                        ajaxRequestTarget.addComponent(usermanagementContainer);
                    }
                });
                userListItem.add(new AjaxLink<User>("user.update") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        input.setUsername(userListItem.getModelObject().getUsername());
                        input.setPassword(userListItem.getModelObject().getPassword());
                        input.setPasswordVerification(userListItem.getModelObject().getPassword());
                        usernameField.setEnabled(false);
                        ajaxRequestTarget.addComponent(usermanagementContainer);
                    }
                });
            }
        };
        users.setOutputMarkupId(true);

        usermanagementContainer.add(users);
        usermanagementContainer.add(userForm);
        add(usermanagementContainer);
    }

    private void createUser() {
        if (input.getUsername() == null) {
            error(new StringResourceModel("usernameError", this, null).getString());
        } else if (input.getPassword() == null || !input.getPassword().equals(input.getPasswordVerification())) {
            error(new StringResourceModel("passwordError", this, null).getString());
        } else {
            try {
                User user = new User(input.getUsername(), input.getPassword());
                userManager.createUser(user);
                info(new StringResourceModel("success", this, null).getString());
                userManager.loadUserByUsername(input.getUsername());
                usernameField.setEnabled(true);
            } catch (UserExistsException ex) {
                error(new StringResourceModel("userExistError", this, null).getString());
                return;
            }
        }
    }


    private class UserInput implements Serializable {

        private String passwordVerification;
        private String password;
        private String username;

        public String getPasswordVerification() {
            return passwordVerification;
        }

        public void setPasswordVerification(String passwordVerification) {
            this.passwordVerification = passwordVerification;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

}