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

package org.openengsb.ui.admin.connectorEditorPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.model.ServiceId;
import org.openengsb.ui.admin.serviceEditor.ServiceEditor;
import org.openengsb.ui.admin.testClient.TestClient;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.openengsb.ui.common.model.LocalizableStringModel;

@AuthorizeInstantiation("ROLE_USER")
public class ConnectorEditorPage extends BasePage {

    @SuppressWarnings("serial")
    private final class ConnectorServiceEditor extends ServiceEditor {

        private ConnectorServiceEditor(String id, List<AttributeDefinition> attributes, Map<String, String> values,
                FormValidator validator) {
            super(id, attributes, values, validator);
        }

        private ConnectorServiceEditor(String id, String serviceId, List<AttributeDefinition> attributes,
                Map<String, String> values, FormValidator validator) {
            super(id, serviceId, attributes, values, validator);
        }

        @Override
        public void onSubmit() {
            boolean checkBoxValue = isValidating();
            if (checkBoxValue) {
                MultipleAttributeValidationResult updateWithValidation =
                    serviceManager.update(idModel.getObject(), getValues());
                if (!updateWithValidation.isValid()) {
                    Map<String, String> attributeErrorMessages =
                        updateWithValidation.getAttributeErrorMessages();
                    for (String value : attributeErrorMessages.values()) {
                        error(new StringResourceModel(value, this, null).getString());
                    }
                } else {
                    returnToTestClient();
                }
            } else {
                serviceManager.updateWithoutValidation(idModel.getObject(), getValues());
                returnToTestClient();
            }
        }

        private void returnToTestClient() {
            String serviceClass = serviceManager.getDescriptor().getServiceType().getName();
            ServiceId reference = new ServiceId(serviceClass, idModel.getObject());
            setResponsePage(new TestClient(reference));
        }
    }

    private final transient InternalServiceRegistrationManager serviceManager;
    private ServiceEditor editor;

    public ConnectorEditorPage(InternalServiceRegistrationManager serviceManager) {
        this.serviceManager = serviceManager;
        HashMap<String, String> attributeValues = new HashMap<String, String>();
        initEditor(attributeValues);
        createEditor(attributeValues);
    }

    public ConnectorEditorPage(InternalServiceRegistrationManager serviceManager, String serviceId) {
        this.serviceManager = serviceManager;
        Map<String, String> attributeValues = serviceManager.getAttributeValues(serviceId);
        initEditor(attributeValues);
        createEditor(attributeValues, serviceId);
    }

    private void initEditor(Map<String, String> attributeValues) {
        ServiceDescriptor descriptor = serviceManager.getDescriptor();
        add(new Label("service.name", new LocalizableStringModel(this, descriptor.getName())));
        add(new Label("service.description", new LocalizableStringModel(this, descriptor.getDescription())));
    }

    private void createEditor(Map<String, String> values) {
        List<AttributeDefinition> attributes = getAttributes(values);
        editor =
            new ConnectorServiceEditor("editor", attributes, values, serviceManager.getDescriptor().getFormValidator());
        add(editor);
    }

    private void createEditor(Map<String, String> values, String serviceId) {
        List<AttributeDefinition> attributes = getAttributes(values);
        editor =
            new ConnectorServiceEditor("editor", serviceId, attributes, values, serviceManager.getDescriptor()
                .getFormValidator());
        add(editor);
    }

    private List<AttributeDefinition> getAttributes(Map<String, String> values) {
        List<AttributeDefinition> attributes = serviceManager.getDescriptor().getAttributes();
        for (AttributeDefinition attribute : attributes) {
            if (!values.containsKey(attribute.getId())) { // do not overwrite attributes with default value
                values.put(attribute.getId(), attribute.getDefaultValue().getString(getSession().getLocale()));
            }
        }
        return attributes;
    }

    public ServiceEditorPanel getEditorPanel() {
        return editor.getServiceEditorPanel();
    }

    @Override
    public String getHeaderMenuItem() {
        return TestClient.class.getSimpleName();
    }

}
