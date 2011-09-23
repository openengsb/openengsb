package org.openengsb.ui.common.usermanagement;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.util.BeanUtils2;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
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

    public UserEditPanel(String id, String username) {
        super(id);
        input.setUsername(username);
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
                createUser();
                afterSubmit();
            }
        };
        container.add(userForm);

        RequiredTextField<String> usernameField = new RequiredTextField<String>("username");
        if (input.getUsername() != null) {
            usernameField.setEnabled(false);
        }
        PasswordTextField passwordField = new PasswordTextField("password");
        PasswordTextField passwordVerficationField = new PasswordTextField("passwordVerification");
        usernameField.setOutputMarkupId(true);
        passwordField.setOutputMarkupId(true);
        passwordVerficationField.setOutputMarkupId(true);

        userForm.add(usernameField);
        userForm.add(passwordField);
        userForm.add(passwordVerficationField);

        userForm.setModel(new CompoundPropertyModel<UserInput>(input));

        IModel<? extends List<? extends PermissionDesc>> permissionsModel =
            new LoadableDetachableModel<List<? extends PermissionDesc>>() {

                private static final long serialVersionUID = 4466384276713173634L;

                @Override
                protected List<? extends PermissionDesc> load() {
                    if (createMode) {
                        return Collections.emptyList();
                    }
                    Collection<Permission> userPermissions;
                    try {
                        userPermissions = userManager.getUserPermissions(input.getUsername());
                    } catch (UserNotFoundException e) {
                        LOGGER.warn("user \"{}\" not found", input.getUsername());
                        return Collections.emptyList();
                    }
                    Collection<PermissionDesc> permissionDescs =
                        Collections2.transform(userPermissions, new Function<Permission, PermissionDesc>() {
                            @Override
                            public PermissionDesc apply(Permission input) {
                                PermissionDesc permissionDesc = new PermissionDesc();
                                permissionDesc.setRepresentation(input.toString());
                                permissionDesc.setDescription(input.describe());
                                return permissionDesc;
                            }
                        });
                    return Lists.newArrayList(permissionDescs);
                }
            };
        ListView<PermissionDesc> listView = new ListView<PermissionDesc>("permissionList", permissionsModel) {

            private static final long serialVersionUID = -8712742630042478882L;

            @Override
            protected void populateItem(ListItem<PermissionDesc> listitem) {
                Label label = new Label("id", new PropertyModel<String>(listitem.getModelObject(), "representation"));
                listitem.add(label);
            }
        };
        userForm.add(listView);
        final WebMarkupContainer createPermissionContainer = new WebMarkupContainer("createPermissionContainer");
        createPermissionContainer.setOutputMarkupId(true);
        userForm.add(createPermissionContainer);
        permissionContentPanel = new EmptyPanel("createPermissionContent");
        createPermissionContainer.add(permissionContentPanel);
        AjaxLink<Object> ajaxButton = new AjaxLink<Object>("createPermission") {
            private static final long serialVersionUID = 6887755633726845337L;

            private boolean shown = false;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (shown) {
                    EmptyPanel emptyPanel = new EmptyPanel("createPermissionContent");
                    createPermissionContainer.addOrReplace(emptyPanel);
                    permissionContentPanel = emptyPanel;
                    target.addComponent(createPermissionContainer);
                    shown = false;
                } else {
                    PermissionEditorPanel realPermissionEditorPanel =
                        new PermissionEditorPanel("createPermissionContent", input);
                    createPermissionContainer.addOrReplace(realPermissionEditorPanel);
                    // permissionContentPanel.replace(realPermissionEditorPanel);
                    permissionContentPanel = realPermissionEditorPanel;
                    target.addComponent(createPermissionContainer);
                    shown = true;
                }
            }
        };
        userForm.add(ajaxButton);

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        userForm.add(feedbackPanel);

    }

    private void createUser() {
        String username = input.getUsername();
        if (username == null) {
            error(new StringResourceModel("usernameError", this, null).getString());
            return;
        }
        if (ObjectUtils.notEqual(input.getPassword(), input.getPasswordVerification())) {
            error(new StringResourceModel("passwordError", this, null).getString());
            return;
        }
        if (createMode) {
            try {
                userManager.createUser(username);
            } catch (UserExistsException e1) {
                error(new StringResourceModel("userExistError", this, null).getString());
                return;
            }
        }
        if (input.getPassword() != null) {
            // TODO require password
            try {
                userManager.setUserCredentials(username, "password", input.getPassword());
            } catch (UserNotFoundException e) {
                error(Throwables.getStackTraceAsString(e));
                return;
            }
        }
        for (PermissionInput p : input.getNewPermissions()) {
            Permission perm = (Permission) BeanUtils2.buildBeanFromAttributeMap(p.getType(), p.getValues());
            try {
                userManager.storeUserPermission(username, perm);
            } catch (UserNotFoundException e) {
                error(Throwables.getStackTraceAsString(e));
                return;
            }
        }
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
