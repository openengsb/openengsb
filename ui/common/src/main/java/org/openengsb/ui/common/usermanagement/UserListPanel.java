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
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.ui.common.panel.ConfirmPanel;
import org.ops4j.pax.wicket.api.PaxWicketBean;

import com.google.common.collect.Lists;

public abstract class UserListPanel extends Panel {

    private static final long serialVersionUID = 4174921735598606946L;

    @PaxWicketBean(name = "userManager")
    private UserDataManager userManager;

    public UserListPanel(String id) {
        super(id);
        initContent();
    }

    private void initContent() {
        final WebMarkupContainer usermanagementContainer = new WebMarkupContainer("usermanagementContainer");
        usermanagementContainer.setOutputMarkupId(true);
        add(usermanagementContainer);
        final Form<Object> form = new Form<Object>("form");
        usermanagementContainer.add(form);
        AjaxButton button = new AjaxButton("createButton", form) {
            private static final long serialVersionUID = 7066662790847828520L;

            @Override
            protected void onSubmit(AjaxRequestTarget arg0, Form<?> arg1) {
                gotoEditorPage(null);
            }
        };
        form.add(button);

        final IModel<List<String>> userList = new LoadableDetachableModel<List<String>>() {
            private static final long serialVersionUID = 2579825372351310299L;

            @Override
            protected List<String> load() {
                Collection<String> usernameList = userManager.getUserList();
                if (usernameList instanceof List) {
                    return (List<String>) usernameList;
                }
                return Lists.newArrayList(usernameList);
            }
        };

        ListView<String> users = new ListView<String>("users", userList) {
            private static final long serialVersionUID = 7628860457238288128L;

            private ListItem<String> activeConfirm;

            @Override
            protected void populateItem(final ListItem<String> userListItem) {
                userListItem.add(new Label("user.name", userListItem.getModelObject()));
                Panel confirm = new EmptyPanel("confirm");
                userListItem.add(confirm);
                userListItem.setOutputMarkupId(true);
                final AjaxLink<String> deleteLink = new AjaxLink<String>("user.delete") {
                    private static final long serialVersionUID = 2004369349622394213L;

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        if (activeConfirm != null) {
                            activeConfirm.get("confirm").replaceWith(new EmptyPanel("confirm"));
                            ajaxRequestTarget.addComponent(activeConfirm);
                            activeConfirm = null;
                        }
                        final Model<String> model = new Model<String>(userListItem.getModelObject());
                        ConfirmPanel confirmPanel = new ConfirmPanel("confirm", model, userListItem) {
                            private static final long serialVersionUID = -1506781103470764246L;

                            @Override
                            protected void onConfirm(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                                userManager.deleteUser(model.getObject());
                                userList.detach();
                                ajaxRequestTarget.addComponent(usermanagementContainer);
                            }

                            @Override
                            protected void onCancel(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                            }
                        };
                        userListItem.get("confirm").replaceWith(confirmPanel);
                        activeConfirm = userListItem;
                        ajaxRequestTarget.addComponent(userListItem);
                    }
                };
                userListItem.add(deleteLink);
                userListItem.add(new AjaxLink<String>("user.update") {
                    private static final long serialVersionUID = -2327085637957255085L;

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        gotoEditorPage(userListItem.getModelObject());
                    }
                });
            }
        };
        users.setOutputMarkupId(true);

        form.add(users);

    }

    public abstract void gotoEditorPage(String name);

}
