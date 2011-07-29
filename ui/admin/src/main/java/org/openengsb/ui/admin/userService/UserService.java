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

package org.openengsb.ui.admin.userService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManagementException;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.api.security.model.User;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

@AuthorizeAction(action = Action.RENDER, roles = "ROLE_ADMIN")
@PaxWicketMountPoint(mountPoint = "users")
public class UserService extends BasePage {

    UserInput input = new UserInput();
    RequiredTextField<String> usernameField;
    PasswordTextField passwordField;
    private PasswordTextField passwordVerficationField;

    @PaxWicketBean
    private UserManager userManager;
    private boolean editMode = false;
    private TextField<String> rolesField;

    public UserService() {
        initContent();
    }

    public UserService(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    private void initContent() {
        final WebMarkupContainer usermanagementContainer = new WebMarkupContainer("usermanagementContainer");
        usermanagementContainer.setOutputMarkupId(true);

        final Form<UserInput> userForm = new Form<UserInput>("form") {
            private static final long serialVersionUID = 6420993810725159979L;

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

        rolesField = new TextField<String>("roles");
        rolesField.setOutputMarkupId(true);

        userForm.add(usernameField);
        userForm.add(passwordField);
        userForm.add(passwordVerficationField);
        userForm.add(rolesField);

        userForm.setModel(new CompoundPropertyModel<UserInput>(input));

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        userForm.add(feedbackPanel);

        AjaxButton resetButton = new AjaxButton("resetbutton") {
            private static final long serialVersionUID = 7997862317008905740L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                input.setPassword("");
                input.setUsername("");
                input.setPasswordVerification("");
                input.setRoles("");
                form.clearInput();
                usernameField.setEnabled(true);
                editMode = false;
                target.addComponent(usermanagementContainer);
            }
        };

        resetButton.setDefaultFormProcessing(false);
        userForm.add(resetButton);

        IModel<List<User>> userList = new LoadableDetachableModel<List<User>>() {
            private static final long serialVersionUID = 2579825372351310299L;

            @Override
            protected List<User> load() {
                try {
                    return userManager.getAllUser();
                } catch (UserManagementException e) {
                    error(new StringResourceModel("userManagementExceptionError", null).getString());
                }
                return new ArrayList<User>();
            }
        };
        ListView<User> users = new ListView<User>("users", userList) {
            private static final long serialVersionUID = 7628860457238288128L;

            @Override
            protected void populateItem(final ListItem<User> userListItem) {
                userListItem.add(new Label("user.name", userListItem.getModelObject().getUsername()));
                userListItem.add(new AjaxLink<User>("user.delete") {
                    private static final long serialVersionUID = 2004369349622394213L;

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            userManager.deleteUser(userListItem.getModelObject().getUsername());
                            getList().remove(userListItem.getModelObject());
                            ajaxRequestTarget.addComponent(usermanagementContainer);
                        } catch (UserManagementException e) {
                            error(new StringResourceModel("userManagementExceptionError", this, null).getString());
                        }
                    }
                });
                userListItem.add(new AjaxLink<User>("user.update") {
                    private static final long serialVersionUID = -2327085637957255085L;

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        input.setUsername(userListItem.getModelObject().getUsername());
                        input.setPassword(userListItem.getModelObject().getPassword());
                        input.setPasswordVerification(userListItem.getModelObject().getPassword());
                        input.setRoles(makeCommaSeparatedList(userListItem.getModelObject().getAuthorities()));
                        editMode = true;
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
        try {
            if (input.getUsername() == null) {
                error(new StringResourceModel("usernameError", this, null).getString());
            } else if (input.getPassword() == null || !input.getPassword().equals(input.getPasswordVerification())) {
                error(new StringResourceModel("passwordError", this, null).getString());
            } else {
                try {
                    List<GrantedAuthority> authorities = authoritiesFromCommaSeparatedList(input.getRoles());
                    User user = new User(input.getUsername(), input.getPassword(), authorities);
                    if (editMode) {
                        userManager.updateUser(user);
                    } else {
                        userManager.createUser(user);
                    }
                    info(new StringResourceModel("success", this, null).getString());
                    userManager.loadUserByUsername(input.getUsername());
                    usernameField.setEnabled(true);
                    editMode = false;
                } catch (UserExistsException ex) {
                    error(new StringResourceModel("userExistError", this, null).getString());
                    return;
                }
            }
        } catch (UserManagementException ex) {
            error(new StringResourceModel("userManagementExceptionError", this, null).getString());
        }

    }

    private class UserInput implements Serializable {
        private static final long serialVersionUID = 8089287572532176946L;

        private String passwordVerification;
        private String password;
        private String username;
        private String roles;

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

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }
    }

    private String makeCommaSeparatedList(Collection<GrantedAuthority> authorities) {
        return StringUtils.join(authorities, ",");
    }

    private List<GrantedAuthority> authoritiesFromCommaSeparatedList(String roleListString) {
        List<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
        if (roleListString == null) {
            return result;
        }
        String[] roles = roleListString.split(",");
        for (String role : roles) {
            result.add(new GrantedAuthorityImpl(role.trim()));
        }
        return result;
    }

}
