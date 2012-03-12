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

import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.openengsb.core.api.ClassloadingDelegate;
import org.openengsb.ui.common.editor.BeanEditorPanel;
import org.openengsb.ui.common.usermanagement.PermissionInput.State;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class PermissionEditorPanel extends Panel {

    private static final long serialVersionUID = 2009943701781924243L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionEditorPanel.class);

    private Panel editorPanel;

    private AjaxButton submitButton;

    private DropDownChoice<Class<?>> permissionTypeChoice;

    private Model<Class<?>> permissionTypeModel;

    private Map<String, String> values = Maps.newHashMap();

    private UserInput user;

    @PaxWicketBean(name = "permissionProviders")
    private List<ClassloadingDelegate> permissionProviders;

    public PermissionEditorPanel(String id, UserInput user) {
        super(id);
        this.user = user;
        init();
    }

    private void init() {
        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);
        container.setOutputMarkupId(true);

        Form<?> form = new Form<Object>("form");
        container.add(form);

        permissionTypeModel = new Model<Class<?>>();
        LoadableDetachableModel<List<Class<?>>> permissionTypeListModel =
            new LoadableDetachableModel<List<Class<?>>>() {
                private static final long serialVersionUID = -3960517945144173704L;

                @Override
                protected List<Class<?>> load() {
                    List<Class<?>> result = Lists.newArrayList();
                    for (ClassloadingDelegate p : permissionProviders) {
                        result.addAll(p.getSupportedTypes());
                    }
                    return result;
                }
            };

        submitButton = new AjaxButton("submitButton") {
            private static final long serialVersionUID = 6787520770396648012L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Class<?> permissionClass = permissionTypeModel.getObject();
                user.getPermissions().add(new PermissionInput(permissionClass, values, State.NEW));
                editorPanel.replaceWith(new EmptyPanel("permissionEditor"));
                submitButton.setVisible(false);
                target.addComponent(container);
                afterSubmit(target, form);
                LOGGER.info("got values {}", values.toString());
            }
        };
        form.add(submitButton);
        submitButton.setVisible(false);

        permissionTypeChoice =
            new DropDownChoice<Class<?>>("permissionTypeSelect", permissionTypeModel, permissionTypeListModel);
        permissionTypeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 5195539410268926662L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                LOGGER.info("selected + " + permissionTypeModel.getObject());
                BeanEditorPanel beanEditorPanel =
                    new BeanEditorPanel("permissionEditor", permissionTypeModel.getObject(), values);
                editorPanel.replaceWith(beanEditorPanel);
                editorPanel = beanEditorPanel;
                submitButton.setVisible(true);
                target.addComponent(container);
            }
        });

        form.add(permissionTypeChoice);

        editorPanel = new EmptyPanel("permissionEditor");

        form.add(editorPanel);
    }

    protected abstract void afterSubmit(AjaxRequestTarget target, Form<?> form);

    public void setPermissionProviders(List<ClassloadingDelegate> permissionProviders) {
        this.permissionProviders = permissionProviders;
    }
}
