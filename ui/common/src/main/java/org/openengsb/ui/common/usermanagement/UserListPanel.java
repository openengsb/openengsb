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

import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.openengsb.core.api.security.service.UserDataManager;
import org.ops4j.pax.wicket.api.PaxWicketBean;

import com.google.common.collect.Lists;

public abstract class UserListPanel extends Panel {

    private final class UserListModel extends LoadableDetachableModel<List<String>> {
        private static final long serialVersionUID = 2579825372351310299L;

        @Override
        protected List<String> load() {
            Collection<String> usernameList = userManager.getUserList();
            if (usernameList instanceof List) {
                return (List<String>) usernameList;
            }
            return Lists.newArrayList(usernameList);
        }
    }

    private static final long serialVersionUID = 4174921735598606946L;

    @PaxWicketBean(name = "userManager")
    private UserDataManager userManager;

    private UserListModel userListModel;

    public UserListPanel(String id) {
        super(id);
        initContent();
    }

    private void initContent() {
       
        AjaxLink<String> addUserButton = new AjaxLink<String>("addUser") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                openCreatePage(target);
            }
        };
        addUserButton.setOutputMarkupId(false);
        add(addUserButton);
        GenericListPanel<String> listPanel = new GenericListPanel<String>("userList") {

            private static final long serialVersionUID = 1340747497564868555L;

            @Override
            protected IModel<List<String>> getListModel() {
                userListModel = new UserListModel();
                return userListModel;
            }

            protected void onDeleteClick(AjaxRequestTarget ajaxRequestTarget, Form<?> form, String param) {
                userManager.deleteUser(param);
                userListModel.detach();
            };

            @Override
            protected void onEditClick(AjaxRequestTarget target, String param) {
                openEditorPage(target, param);
            }
        };
        add(listPanel);
    }

    protected abstract void openCreatePage(AjaxRequestTarget target);

    protected abstract void openEditorPage(AjaxRequestTarget target, String user);
}
