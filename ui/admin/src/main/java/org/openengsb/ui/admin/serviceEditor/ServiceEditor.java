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
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.openengsb.ui.common.validation.DefaultPassingFormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "SERVICE_EDITOR")
public abstract class ServiceEditor extends Panel {

    private static final long serialVersionUID = 1172948737509752463L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEditor.class);

    private final List<AttributeDefinition> attributes;
    private final FormValidator validator;
    private ServiceEditorPanel serviceEditorPanel;
    protected Model<ConnectorDefinition> idModel;
    private TextField<String> idfield;
    protected Map<String, Object> properties;

    public ServiceEditor(String id, ConnectorDefinition serviceId, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Map<String, Object> properties, FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.validator = validator;
        idModel = new Model<ConnectorDefinition>(serviceId);
        createForm(attributes, attributeMap, properties);
        idfield.setEnabled(false);
    }

    public ServiceEditor(String id, ConnectorDefinition serviceId, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Map<String, Object> properties) {
        this(id, serviceId, attributes, attributeMap, properties, new DefaultPassingFormValidator());
    }

    public ServiceEditor(String id, String domainType, String connectorType, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Map<String, Object> properties, FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.validator = validator;
        idModel = new Model<ConnectorDefinition>(ConnectorDefinition.generate(domainType, connectorType));
        createForm(attributes, attributeMap, properties);
    }

    public ServiceEditor(String id, String domainType, String connectorType, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Map<String, Object> properties) {
        this(id, domainType, connectorType, attributes, attributeMap, properties, new DefaultPassingFormValidator());
    }

    @SuppressWarnings("serial")
    private void createForm(List<AttributeDefinition> attributes, Map<String, String> attributeMap,
            Map<String, Object> properties) {
        this.properties = properties;
        @SuppressWarnings("rawtypes")
        final Form<?> form = new Form("form");
        add(form);
        idfield = new TextField<String>("serviceId", new PropertyModel<String>(idModel.getObject(), "instanceId"));
        idfield.setRequired(true);
        form.add(idfield);

        serviceEditorPanel = new ServiceEditorPanel("attributesPanel", attributes, attributeMap, properties, form);
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
                String newKey = newKeyModel.getObject();
                if (newKey == null) {
                    return;
                }
                Map<String, Object> properties = ServiceEditor.this.properties;
                if (properties.get(newKey) != null) {
                    error("property with the name already exists");
                    return;
                }
                properties.put(newKey, "new Value");
                newKeyModel.setObject("");
                serviceEditorPanel.reloadList(ServiceEditor.this.properties);
                target.add(serviceEditorPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error occured during add property action.");
            }

        });

        form.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ServiceEditor.this.onSubmit();
                if (hasErrorMessage()) {
                    ServiceEditorPanel.addAjaxValidationToForm(form);
                    target.add(form);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                ServiceEditorPanel.addAjaxValidationToForm(form);
                target.add(form);
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
