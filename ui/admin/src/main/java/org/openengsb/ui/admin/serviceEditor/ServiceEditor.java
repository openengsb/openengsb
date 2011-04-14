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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.core.common.util.DictionaryAsMap;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.openengsb.ui.common.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public abstract class ServiceEditor extends Panel {

    private final List<AttributeDefinition> attributes;
    private final FormValidator validator;
    private ServiceEditorPanel serviceEditorPanel;
    protected Model<ConnectorId> idModel;
    private TextField<String> idfield;

    public ServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
            IModel<ConnectorDescription> model, FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.validator = validator;
        idModel = new Model<ConnectorId>(serviceId);

        Map<String, IModel<String>> values = createModelMap(attributes, model);
        IModel<List<? extends Entry<String, Object>>> model2 = createPropertyModel(model);
        createForm(attributes, values, model2);
        idfield.setEnabled(false);
    }

    private IModel<List<? extends Entry<String, Object>>> createPropertyModel(IModel<ConnectorDescription> model) {
        Dictionary<String, Object> properties = model.getObject().getProperties();
        Map<String, Object> wrappedProps = DictionaryAsMap.wrap(properties);
        List<Map.Entry<String, Object>> list = new ArrayList<Map.Entry<String, Object>>(wrappedProps.entrySet());
        return Model.ofList(list);
    }

    public ServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
            IModel<ConnectorDescription> model) {
        this(id, serviceId, attributes, model, new DefaultPassingFormValidator());
    }

    public ServiceEditor(String id, String domainType, String connectorType, List<AttributeDefinition> attributes,
            IModel<ConnectorDescription> model, FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.validator = validator;
        idModel = new Model<ConnectorId>(ConnectorId.generate(domainType, connectorType));
        createForm(attributes, createModelMap(attributes, model), createPropertyModel(model));
    }

    public ServiceEditor(String id, String domainType, String connectorType, List<AttributeDefinition> attributes,
            IModel<ConnectorDescription> model) {
        this(id, domainType, connectorType, attributes, model, new DefaultPassingFormValidator());
    }

    private Map<String, IModel<String>> createModelMap(List<AttributeDefinition> attributes,
            IModel<ConnectorDescription> model) {
        Map<String, IModel<String>> models = new HashMap<String, IModel<String>>();
        for (AttributeDefinition a : attributes) {
            ConnectorAttributeModel model2 = new ConnectorAttributeModel(model, a.getId());
            models.put(a.getId(), model2);
        }
        return models;
    }

    private void createForm(List<AttributeDefinition> attributes, Map<String, IModel<String>> values,
            IModel<List<? extends Entry<String, Object>>> model) {
        @SuppressWarnings("rawtypes")
        final Form<?> form = new Form("form");
        add(form);
        idfield = new TextField<String>("serviceId", new PropertyModel<String>(idModel.getObject(), "instanceId"));
        idfield.setRequired(true);
        form.add(idfield);

        serviceEditorPanel = new ServiceEditorPanel("attributesPanel", attributes, values, model);
        form.add(serviceEditorPanel);

        if (validator != null) {
            serviceEditorPanel.attachFormValidator(form, validator);
        }

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
