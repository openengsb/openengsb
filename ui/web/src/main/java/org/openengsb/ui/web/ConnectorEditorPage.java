/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.AttributeDefinition.Builder;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.ui.web.editor.EditorPanel;
import org.openengsb.ui.web.model.LocalizableStringModel;
import org.openengsb.ui.web.model.ServiceId;
import org.openengsb.ui.web.model.WicketStringLocalizer;

public class ConnectorEditorPage extends BasePage {

    private final transient ServiceManager serviceManager;
    private EditorPanel editorPanel;

    public ConnectorEditorPage(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        ServiceDescriptor descriptor = serviceManager.getDescriptor();
        add(new Label("service.name", new LocalizableStringModel(this, descriptor.getName())));
        add(new Label("service.description", new LocalizableStringModel(this, descriptor.getDescription())));
        createEditor(new HashMap<String, String>());
    }

    public ConnectorEditorPage(ServiceManager serviceManager, String serviceId) {
        this.serviceManager = serviceManager;
        Map<String, String> attributeValues = serviceManager.getAttributeValues(serviceId);
        ServiceDescriptor descriptor = serviceManager.getDescriptor();
        add(new Label("service.name", new LocalizableStringModel(this, descriptor.getName())));
        add(new Label("service.description", new LocalizableStringModel(this, descriptor.getDescription())));
        createEditor(attributeValues);
    }

    @SuppressWarnings("serial")
    private void createEditor(Map<String, String> values) {
        List<AttributeDefinition> attributes = buildAttributeList(serviceManager);
        for (AttributeDefinition attribute : attributes) {
            if (!values.containsKey(attribute.getId())) { // do not overwrite attributes with default value
                values.put(attribute.getId(), attribute.getDefaultValue().getString(getSession().getLocale()));
            }
        }
        editorPanel = new EditorPanel("editor", attributes, values, serviceManager.getDescriptor().getFormValidator()) {
            @Override
            public void onSubmit() {
                CheckBox component = (CheckBox) editorPanel.get("form:validate");
                boolean checkBoxValue = component.getModelObject();
                if (checkBoxValue) {
                    MultipleAttributeValidationResult updateWithValidation =
                        serviceManager.update(getValues().get("id"), getValues());
                    if (!updateWithValidation.isValid()) {
                        Map<String, String> attributeErrorMessages = updateWithValidation.getAttributeErrorMessages();
                        for (String value : attributeErrorMessages.values()) {
                            error(new StringResourceModel(value, this, null).getString());
                        }
                    } else {
                        String serviceClass = serviceManager.getDescriptor().getServiceType().getName();
                        String id = getValues().get("id");
                        ServiceId reference = new ServiceId(serviceClass, id);
                        setResponsePage(new TestClient(reference));
                        // info(new StringResourceModel("service.add.succeed", this, null).getString());
                    }
                } else {
                    serviceManager.update(getValues().get("id"), getValues());
                    info(new StringResourceModel("service.add.succeed", this, null).getString());
                }
            }
        };
        add(editorPanel);
    }

    private List<AttributeDefinition> buildAttributeList(ServiceManager service) {
        Builder builder = AttributeDefinition.builder(new WicketStringLocalizer(this));
        AttributeDefinition id = builder.id("id").name("attribute.id.name").description("attribute.id.description")
                .required().build();
        ServiceDescriptor descriptor = service.getDescriptor();
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.add(id);
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    @Override
    public String getHeaderMenuItem() {
        return TestClient.class.getSimpleName();
    }

}
