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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
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

    private ModalWindow editModalWindow;

    private static final long serialVersionUID = -6841313899998597640L;

    public static final String PAGE_NAME_KEY = "userListPage.title";
    public static final String PAGE_DESCRIPTION_KEY = "userListPage.description";

    private static final int EDIT_MODAL_WINDOW_INITIAL_HEIGHT = 500;
    private static final int EDIT_MODAL_WINDOW_INITIAL_WIDTH = 530;
    
    public UserListPage() {
        super(PAGE_NAME_KEY);
        initContent();
    }

    public UserListPage(PageParameters parameters) {
        super(parameters, PAGE_NAME_KEY);
        initContent();
    }

    private void initContent() {
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        add(new MyUserListPanel("lazy"));
        editModalWindow = new ModalWindow("editModalWindow");
        editModalWindow.setCookieName("editModalWindow");
        editModalWindow.setInitialHeight(EDIT_MODAL_WINDOW_INITIAL_HEIGHT);
        editModalWindow.setInitialWidth(EDIT_MODAL_WINDOW_INITIAL_WIDTH);
        add(editModalWindow);
    }

    private final class MyUserListPanel extends UserListPanel {
        private static final long serialVersionUID = 4561294480791309137L;
        
        private MyUserListPanel(String id) {
            super(id);
        }

        @Override
        protected void openCreatePage(AjaxRequestTarget target) {
            editModalWindow.setTitle(getLocalizer().getString("add.user", this));
            editModalWindow.setContent(new EditPanel(editModalWindow.getContentId()));
            editModalWindow.show(target);
        }

        @Override
        protected void openEditorPage(AjaxRequestTarget target, String user) {
            editModalWindow.setTitle(getLocalizer().getString("edit.user", this));
            editModalWindow.setContent(new EditPanel(editModalWindow.getContentId(), user));
            editModalWindow.show(target);
        }
    }

    private final class EditPanel extends UserEditPanel {
        private static final long serialVersionUID = -4646745795328499771L;

        private EditPanel(String id) {
            super(id);
        }

        public EditPanel(String id, String username) throws UserNotFoundException {
            super(id, username);
        }

        @Override
        protected void afterSubmit() {
        }
    }
}
