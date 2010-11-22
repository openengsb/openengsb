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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
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

import java.util.List;

@AuthorizeInstantiation("ROLE_USER")
public class UserService extends BasePage {

    private static Log log = LogFactory.getLog(UserService.class);
    private User user = new User(null, null);

    @SpringBean
    private UserManager userManager;

    public UserService() {
        Form<User> userForm = new Form<User>("form") {
            @Override
            protected void onSubmit() {
                if (user.getUsername() == null) {
                    error(new StringResourceModel("usernameError", this, null).getString());
                } else if (user.getPassword() == null) {
                    error(new StringResourceModel("passwordError", this, null).getString());
                } else {
                    try {
                        userManager.createUser(user);
                        info(new StringResourceModel("success", this, null).getString());
                        userManager.loadUserByUsername(user.getUsername());
                    } catch (UserExistsException ex) {
                        error(new StringResourceModel("userExistError", this, null).getString());
                        return;
                    }
                }
            }
        };
        userForm.add(new RequiredTextField<String>("username"));
        userForm.add(new PasswordTextField("password"));
        userForm.setModel(new CompoundPropertyModel<User>(user));
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        add(feedbackPanel);
        add(userForm);


        IModel<List<User>> userList = new LoadableDetachableModel<List<User>>() {
            @Override
            protected List<User> load() {
                return userManager.getAllUser();
            }
        };
        ListView<User> users = new ListView<User>("users",userList) {
            @Override
            protected void populateItem(final ListItem<User> userListItem) {
                userListItem.add(new Label("user.name", userListItem.getModelObject().getUsername()));
                userListItem.add(new AjaxLink<User>("user.delete") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        userManager.deleteUser(userListItem.getModelObject().getUsername());
                    }
                });
            }
        };
        add(users);
    }

}