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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.ServiceValidationFailedException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.serviceEditor.ServiceEditor;
import org.openengsb.ui.admin.testClient.TestClient;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.openengsb.ui.common.model.LocalizableStringModel;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

@AuthorizeInstantiation("ROLE_USER")
public class ConnectorEditorPage extends BasePage {

    @SpringBean
    private ServiceManager serviceManager;

    private ServiceDescriptor descriptor;

    private ServiceEditor editor;

    private OsgiUtilsService serviceUtils = OpenEngSBCoreServices.getServiceUtilsService();

    @SuppressWarnings("serial")
    private final class ConnectorServiceEditor extends ServiceEditor {

        private ConnectorServiceEditor(String id, String domainType, String connectorType,
                List<AttributeDefinition> attributes, Map<String, String> values,
                FormValidator validator) {
            super(id, domainType, connectorType, attributes, values, validator);
        }

        private ConnectorServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
                Map<String, String> values, FormValidator validator) {
            super(id, serviceId, attributes, values, validator);
        }

        @Override
        public void onSubmit() {
            ConnectorDescription connectorDescription = new ConnectorDescription();
            connectorDescription.setAttributes(getValues());
            try {
                serviceManager.update(idModel.getObject(), connectorDescription); // , isValidating());
                returnToTestClient();
            } catch (ServiceValidationFailedException e) {
                for (String value : e.getErrorMessages().values()) {
                    error(new StringResourceModel(value, this, null).getString());
                }
            }
        }

        private void returnToTestClient() {
            // String serviceClass = serviceManager.getDescriptor().getServiceType().getName();
            // ServiceId reference = new ServiceId(serviceClass, idModel.getObject());
            // setResponsePage(new TestClient(reference));
            setResponsePage(TestClient.class);
        }
    }

    public ConnectorEditorPage(String domain, String connectorType) {
        retrieveDescriptor(connectorType);
        HashMap<String, String> attributeValues = new HashMap<String, String>();
        initEditor(connectorType, attributeValues);
        createEditor(domain, connectorType, attributeValues);
    }

    private void retrieveDescriptor(String connectorType) {
        Filter filter;
        try {
            filter = serviceUtils.makeFilter(ConnectorProvider.class, String.format("(connector=%s)", connectorType));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        ConnectorProvider connectorProvider = serviceUtils.getOsgiServiceProxy(filter, ConnectorProvider.class);
        descriptor = connectorProvider.getDescriptor();
    }

    public ConnectorEditorPage(ConnectorId id) {
        retrieveDescriptor(id.getConnectorType());
        ConnectorDescription connectorDesc = serviceManager.getAttributeValues(id);
        initEditor(id.getConnectorType(), connectorDesc.getAttributes());
        createEditor(connectorDesc.getAttributes(), id);
    }

    private void initEditor(String connectorType, Map<String, String> attributeValues) {
        add(new Label("service.name", new LocalizableStringModel(this, descriptor.getName())));
        add(new Label("service.description", new LocalizableStringModel(this, descriptor.getDescription())));
    }

    private void createEditor(String domainType, String connectorType, Map<String, String> values) {
        List<AttributeDefinition> attributes = getAttributes(values);
        editor =
            new ConnectorServiceEditor("editor", domainType, connectorType, attributes, values,
                descriptor.getFormValidator());
        add(editor);
    }

    private void createEditor(Map<String, String> values, ConnectorId serviceId) {
        List<AttributeDefinition> attributes = getAttributes(values);
        editor = new ConnectorServiceEditor("editor", serviceId, attributes, values, descriptor.getFormValidator());
        add(editor);
    }

    private List<AttributeDefinition> getAttributes(Map<String, String> values) {
        List<AttributeDefinition> attributes = descriptor.getAttributes();
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
