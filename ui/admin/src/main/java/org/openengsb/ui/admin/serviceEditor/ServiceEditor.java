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

package org.openengsb.ui.admin.serviceEditor;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.openengsb.ui.common.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public abstract class ServiceEditor extends Panel {

    private final List<AttributeDefinition> attributes;
    private final FormValidator validator;
    private ServiceEditorPanel serviceEditorPanel;
    protected Model<ConnectorId> idModel;
    private TextField<String> idfield;
    protected Dictionary<String, Object> properties;

    public ServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Dictionary<String, Object> properties, FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.validator = validator;
        idModel = new Model<ConnectorId>(serviceId);
        createForm(attributes, attributeMap, properties);
        idfield.setEnabled(false);
    }

    public ServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Dictionary<String, Object> properties) {
        this(id, serviceId, attributes, attributeMap, properties, new DefaultPassingFormValidator());
    }

    public ServiceEditor(String id, String domainType, String connectorType, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Dictionary<String, Object> properties, FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.validator = validator;
        idModel = new Model<ConnectorId>(ConnectorId.generate(domainType, connectorType));
        createForm(attributes, attributeMap, properties);
    }

    public ServiceEditor(String id, String domainType, String connectorType, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Dictionary<String, Object> properties) {
        this(id, domainType, connectorType, attributes, attributeMap, properties, new DefaultPassingFormValidator());
    }

    private void createForm(List<AttributeDefinition> attributes, Map<String, String> attributeMap,
            Dictionary<String, Object> properties) {
        this.properties = properties;
        @SuppressWarnings("rawtypes")
        final Form<?> form = new Form("form");
        add(form);
        idfield = new TextField<String>("serviceId", new PropertyModel<String>(idModel.getObject(), "instanceId"));
        idfield.setRequired(true);
        form.add(idfield);

        serviceEditorPanel = new ServiceEditorPanel("attributesPanel", attributes, attributeMap, properties);
        form.add(serviceEditorPanel);

        if (validator != null) {
            serviceEditorPanel.attachFormValidator(form, validator);
        }
        serviceEditorPanel.setOutputMarkupId(true);

        final IModel<String> newKeyModel = new Model<String>();
        TextField<String> textField = new TextField<String>("newPropertyKey", newKeyModel);
        form.add(textField);

        form.add(new AjaxButton("addProperty", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ServiceEditor.this.properties.put(newKeyModel.getObject(), "new Value");
                serviceEditorPanel.reloadList(ServiceEditor.this.properties);
                target.addComponent(serviceEditorPanel);
            }
        });

        form.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ServiceEditor.this.onSubmit();
                if (hasErrorMessage()) {
                    ServiceEditorPanel.addAjaxValidationToForm(form);
                    target.addComponent(form);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                ServiceEditorPanel.addAjaxValidationToForm(form);
                target.addComponent(form);
            }
        };
        form.setOutputMarkupId(true);
        form.add(submitButton);
    }

    public abstract void onSubmit();

    public ServiceEditorPanel getServiceEditorPanel() {
        return serviceEditorPanel;
    }

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    protected boolean isValidating() {
        return serviceEditorPanel.isValidating();
    }
}
