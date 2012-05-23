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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.common.usermanagement.UserEditPanel;
import org.openengsb.ui.common.usermanagement.UserListPanel;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "USER_ADMIN")
@PaxWicketMountPoint(mountPoint = "users")
public class UserListPage extends BasePage {

    Panel userDialogue;

    private static final long serialVersionUID = -6841313899998597640L;
    
    public static final String pageNameKey="userListPage.title";
    public static final String pageDescriptionKey="userListPage.description";

    private final class MyUserListPanel extends UserListPanel {
        private static final long serialVersionUID = 4561294480791309137L;

        private MyUserListPanel(String id) {
            super(id);
        }

        @Override
        protected void openCreatePage(AjaxRequestTarget target) {
            EditPanel createUser = new EditPanel("userDialogue");  
            createUser.setOutputMarkupId(true);
            userDialogue.replaceWith(createUser);
            userDialogue = createUser;	
            target.add(userDialogue);
            target.appendJavaScript("showModalDialogue('" + createUser.getMarkupId()
                    + "','" + getLocalizer().getString("add.user", this) + "',false,500,400);");
            
        }

        @Override
        protected void openEditorPage(AjaxRequestTarget target, String user) {
            EditPanel editUser = new EditPanel("userDialogue", user);  
            editUser.setOutputMarkupId(true);
            userDialogue.replaceWith(editUser);
            userDialogue = editUser;
            target.add(userDialogue);
            target.appendJavaScript("showModalDialogue('" + editUser.getMarkupId()
                + "','" + getLocalizer().getString("edit.user", this) + ": " + user + "',false,500,400);");
        }
    }
    
    //TODO CHECK IF NEEDED? //
    private final class EditPanel extends UserEditPanel {
        private static final long serialVersionUID = -4646745795328499771L;

        private EditPanel(String id) {
            super(id);
        }

        protected void openEditorPage(String user) {
            PageParameters parameters = new PageParameters();
            parameters.set("user", user);
            setResponsePage(UserEditPage.class, parameters);
        }

        public EditPanel(String id, String username) throws UserNotFoundException {
            super(id, username);
        }

        @Override
        protected void afterSubmit() {
            setResponsePage(UserListPage.class);
        }
    }

    public UserListPage() {
        super(pageNameKey);
    	initContent();
    }

    public UserListPage(PageParameters parameters) {
        super(parameters, pageNameKey);
        initContent();
    }

    private void initContent() {
        add(new MyUserListPanel("lazy"));
        //Panel for Modal Dialog for User add/edit form
        userDialogue = new EmptyPanel("userDialogue");
        userDialogue.setOutputMarkupId(true);
        add(userDialogue);
    }
}
