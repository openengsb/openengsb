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
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.openengsb.ui.common.panel.ConfirmPanel;

public abstract class GenericListPanel<T extends Serializable> extends Panel {

    private static final long serialVersionUID = 4174921735598606946L;
    private IModel<List<T>> listModel;

    public GenericListPanel(String id) {
        super(id);
        this.listModel = getListModel();
        initContent();
    }

    protected abstract IModel<List<T>> getListModel();

    private void initContent() {
        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);
        final Form<Object> form = new Form<Object>("form");
        listContainer.add(form);
        final Panel confirm = new EmptyPanel("confirm");
        confirm.setOutputMarkupId(true);
        listContainer.add(confirm);

        ListView<T> users = new ListView<T>("list", listModel) {
            private static final long serialVersionUID = 7628860457238288128L;

            @Override
            protected void populateItem(final ListItem<T> userListItem) {
                userListItem.add(new Label("item.name", userListItem.getModelObject().toString()));
                userListItem.setOutputMarkupId(true);
                final AjaxLink<String> deleteLink = new AjaxLink<String>("item.delete") {
                    private static final long serialVersionUID = 2004369349622394213L;
                    
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        
                        final Model<T> model = new Model<T>(userListItem.getModelObject());
                        ConfirmPanel<T> confirmPanel = new ConfirmPanel<T>("confirm", model, userListItem.get("item.name").toString()) {
                            private static final long serialVersionUID = -1506781103470764246L;

							@Override
							protected void onSuccess(AjaxRequestTarget target) {
								onDeleteClick(target, form, model.getObject());
                 			}

							@Override
							protected void onFailure(AjaxRequestTarget target) {
								target.add(listContainer);
							}
                        };
                        listContainer.get("confirm").replaceWith(confirmPanel);
                        ajaxRequestTarget.add(listContainer);
                        confirmPanel.showDialog();
                    }
                };
                deleteLink.setOutputMarkupId(true);
                userListItem.add(deleteLink);
                userListItem.add(new AjaxLink<String>("item.update") {
                    private static final long serialVersionUID = -2327085637957255085L;

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        onEditClick(ajaxRequestTarget, userListItem.getModelObject());
                    }
                });
            }
        };
        users.setOutputMarkupId(true);
        form.add(users);
    }

    protected abstract void onDeleteClick(AjaxRequestTarget ajaxRequestTarget, Form<?> form, T param);

    protected abstract void onEditClick(AjaxRequestTarget target, T param);

}
