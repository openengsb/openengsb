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
package org.openengsb.ui.common.usermanagement;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.util.BeanUtilsExtended;
import org.openengsb.ui.common.usermanagement.PermissionInput.State;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public abstract class UserEditPanel extends Panel {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEditPanel.class);

    private static final long serialVersionUID = 5587225457795713881L;

    @PaxWicketBean(name = "userManager")
    private UserDataManager userManager;

    private boolean createMode = true;

    private UserInput input = new UserInput();

    private Panel permissionContentPanel;

    public UserEditPanel(String id) {
        super(id);
        initContent();
    }

    public UserEditPanel(String id, String username) throws UserNotFoundException {
        super(id);
        input.setUsername(username);
        Collection<Permission> userPermissions = userManager.getPermissionsForUser(username);
        Collection<PermissionInput> permissionInput =
            Collections2.transform(userPermissions, new Function<Permission, PermissionInput>() {
                @Override
                public PermissionInput apply(Permission input) {
                    Map<String, String> values = BeanUtilsExtended.buildStringAttributeMap(input);
                    return new PermissionInput(input.getClass(), values);
                }
            });
        input.setPermissions(Lists.newLinkedList(permissionInput));
        createMode = false;
        initContent();
    }

    private void initContent() {
        final WebMarkupContainer container = new WebMarkupContainer("userEditorContainer");
        add(container);
        Form<UserInput> userForm = new Form<UserInput>("userForm") {
            private static final long serialVersionUID = 6420993810725159979L;

            @Override
            protected void onSubmit() {
                if (createUser()) {
                    afterSubmit();
                }
            }
        };
        container.add(userForm);

        RequiredTextField<String> usernameField = new RequiredTextField<String>("username");
        if (input.getUsername() != null) {
            usernameField.setEnabled(false);
        }
        PasswordTextField passwordField = new PasswordTextField("password");
        passwordField.setRequired(false);
        PasswordTextField passwordVerficationField = new PasswordTextField("passwordVerification");
        passwordVerficationField.setRequired(false);
        usernameField.setOutputMarkupId(true);
        passwordField.setOutputMarkupId(true);
        passwordVerficationField.setOutputMarkupId(true);

        userForm.add(usernameField);
        userForm.add(passwordField);
        userForm.add(passwordVerficationField);

        userForm.setModel(new CompoundPropertyModel<UserInput>(input));

        final WebMarkupContainer permissionListContainer = new WebMarkupContainer("permissionListContainer");
        permissionListContainer.setOutputMarkupId(true);
        userForm.add(permissionListContainer);

        final WebMarkupContainer createPermissionContainer = new WebMarkupContainer("createPermissionContainer");
        createPermissionContainer.setOutputMarkupId(true);
        permissionListContainer.add(createPermissionContainer);

        permissionContentPanel = new EmptyPanel("createPermissionContent");
        createPermissionContainer.add(permissionContentPanel);

        GenericListPanel<PermissionInput> permissionList = new GenericListPanel<PermissionInput>("permissionList") {
            private static final long serialVersionUID = 8972059517453578207L;

            @Override
            protected IModel<List<PermissionInput>> getListModel() {
                return new LoadableDetachableModel<List<PermissionInput>>() {
                    private static final long serialVersionUID = 5621251753383842382L;

                    @Override
                    protected List<PermissionInput> load() {
                        Collection<PermissionInput> filtered =
                            Collections2.filter(input.getPermissions(), new Predicate<PermissionInput>() {
                                @Override
                                public boolean apply(PermissionInput input) {
                                    return input.getState() != State.DELETED;
                                }
                            });
                        return Lists.newArrayList(filtered);
                    }
                };
            }

            @Override
            protected void onDeleteClick(AjaxRequestTarget ajaxRequestTarget, Form<?> form, PermissionInput param) {
                if (param.getState() == State.NEW) {
                    input.getPermissions().remove(param);
                    return;
                }
                param.setState(State.DELETED);
            }

            @Override
            protected void onEditClick(AjaxRequestTarget target, PermissionInput param) {
                LOGGER.info("would edit " + param);
            }

        };
        permissionListContainer.add(permissionList);

        AjaxLink<Object> ajaxButton = new AjaxLink<Object>("createPermission") {
            private static final long serialVersionUID = 6887755633726845337L;

            private void reenable() {
                setEnabled(true);
                // setVisible(true);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                PermissionEditorPanel realPermissionEditorPanel =
                    new PermissionEditorPanel("createPermissionContent", input) {
                        private static final long serialVersionUID = 6316566908822500463L;

                        @Override
                        protected void afterSubmit(AjaxRequestTarget target, Form<?> form) {
                            LOGGER.info("replacing permission-panel with empty panel");
                            EmptyPanel emptyPanel = new EmptyPanel("createPermissionContent");
                            createPermissionContainer.addOrReplace(emptyPanel);
                            permissionContentPanel = emptyPanel;
                            target.add(createPermissionContainer);
                            reenable();
                            target.add(permissionListContainer);
                        }
                    };
                createPermissionContainer.addOrReplace(realPermissionEditorPanel);
                permissionContentPanel = realPermissionEditorPanel;
                target.add(createPermissionContainer);
                setEnabled(false);
                target.add(this);
            }
        };
        permissionListContainer.add(ajaxButton);

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        userForm.add(feedbackPanel);

    }

    private boolean createUser() {
        String username = input.getUsername();
        if (username == null) {
            error(new StringResourceModel("usernameError", this, null).getString());
            return false;
        }
        if (ObjectUtils.notEqual(input.getPassword(), input.getPasswordVerification())) {
            error(new StringResourceModel("passwordError", this, null).getString());
            return false;
        }
        if (createMode) {
            if (input.getPassword() == null) {
                error(new StringResourceModel("passwordError", this, null).getString());
                return false;
            }
            try {
                userManager.createUser(username);
            } catch (UserExistsException e1) {
                error(new StringResourceModel("userExistError", this, null).getString());
                return false;
            }
        }
        if (input.getPassword() != null) {
            try {
                userManager.setUserCredentials(username, "password", input.getPassword());
            } catch (UserNotFoundException e) {
                error(Throwables.getStackTraceAsString(e));
                return false;
            }
        }
        for (PermissionInput p : input.getPermissions()) {
            if (p.getState() == State.UNMODIFIED) {
                continue;
            }
            Permission perm = (Permission) BeanUtilsExtended.createBeanFromAttributeMap(p.getType(), p.getValues());
            try {
                if (p.getState() == State.NEW) {
                    userManager.addPermissionToUser(username, perm);
                }
                if (p.getState() == State.DELETED) {
                    userManager.removePermissionFromUser(username, perm);
                }
            } catch (UserNotFoundException e) {
                error(Throwables.getStackTraceAsString(e));
                return false;
            }
        }
        return true;
    }

    protected abstract void afterSubmit();

    class PermissionDesc implements Serializable {

        private static final long serialVersionUID = -4440114734798836870L;
        private String representation;
        private String description;

        public String getRepresentation() {
            return representation;
        }

        public void setRepresentation(String representation) {
            this.representation = representation;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

}
