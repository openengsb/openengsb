package org.openengsb.ui.admin.userService;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManagementException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.ops4j.pax.wicket.api.PaxWicketBean;

public abstract class UserEditPanel extends Panel {

    private static final long serialVersionUID = 5587225457795713881L;

    @PaxWicketBean(name = "userManager")
    private UserDataManager userManager;

    private UserInput input = new UserInput();

    public UserEditPanel(String id) {
        super(id);
        initContent();
    }

    public UserEditPanel(String id, String username) {
        super(id);
        input.setUsername(username);
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

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        userForm.add(feedbackPanel);

    }

    private void createUser() {
        try {
            String username = input.getUsername();
            if (username == null) {
                error(new StringResourceModel("usernameError", this, null).getString());
            } else if (input.getPassword() == null || !input.getPassword().equals(input.getPasswordVerification())) {
                error(new StringResourceModel("passwordError", this, null).getString());
            } else {
                try {
                    userManager.createUser(username);
                    userManager.setUserCredentials(username, "password", input.getPassword());
                    info(new StringResourceModel("success", this, null).getString());
                } catch (UserExistsException ex) {
                    error(new StringResourceModel("userExistError", this, null).getString());
                    return;
                } catch (UserNotFoundException e) {
                    error(new StringResourceModel("userExistError", this, null).getString());
                    return;
                }
            }
        } catch (UserManagementException ex) {
            error(new StringResourceModel("userManagementExceptionError", this, null).getString());
        }

    }
    
    protected abstract void afterSubmit();

    class UserInput implements Serializable {
        private static final long serialVersionUID = 8089287572532176946L;

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
