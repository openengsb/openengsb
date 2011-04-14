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
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
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

        private boolean createMode;
        private IModel<ConnectorDescription> descriptionModel;

        private ConnectorServiceEditor(String id, String domainType, String connectorType,
                List<AttributeDefinition> attributes, IModel<ConnectorDescription> model, FormValidator validator) {
            super(id, domainType, connectorType, attributes, model, validator);
            this.descriptionModel = model;
            createMode = true;
        }

        private ConnectorServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
                IModel<ConnectorDescription> model, FormValidator validator) {
            super(id, serviceId, attributes, model, validator);
            this.descriptionModel = model;
            createMode = false;
        }

        @Override
        public void internalOnSubmit() {
            ConnectorDescription connectorDescription = descriptionModel.getObject();
            try {
                if (createMode) {
                    serviceManager.createService(idModel.getObject(), connectorDescription);
                } else {
                    serviceManager.update(idModel.getObject(), connectorDescription); // , isValidating());
                }
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
        initEditor(connectorType);
        createEditor(domain, connectorType);
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
        // ConnectorDescription connectorDesc = serviceManager.getAttributeValues(id);
        initEditor(id.getConnectorType());
        createEditor(id);
    }

    public ConnectorEditorPage(PageParameters parameters) {
        super(parameters);
        String serviceId = parameters.getString("id");
        String domainType = parameters.getString("domainType");
        String connectorType = parameters.getString("connectorType");
        ConnectorId connectorId = new ConnectorId(domainType, connectorType, serviceId);
        retrieveDescriptor(connectorType);
        initEditor(connectorType);
        createEditor(connectorId);
    }

    private void initEditor(String connectorType) {
        add(new Label("service.name", new LocalizableStringModel(this, descriptor.getName())));
        add(new Label("service.description", new LocalizableStringModel(this, descriptor.getDescription())));
    }

    private void createEditor(String domainType, String connectorType) {
        List<AttributeDefinition> attributes = descriptor.getAttributes();
        Map<String, String> values = new HashMap<String, String>();
        for (AttributeDefinition def : attributes) {
            if (def.getDefaultValue() != null) {
                String value = def.getDefaultValue().getString(Locale.getDefault());
                values.put(def.getId(), value);
            }
        }
        ConnectorDescription description = new ConnectorDescription(values);
        Model<ConnectorDescription> model = new Model<ConnectorDescription>(description);
        editor =
            new ConnectorServiceEditor("editor", domainType, connectorType, attributes, model,
                descriptor.getFormValidator());
        add(editor);
    }

    private void createEditor(final ConnectorId serviceId) {

        IModel<ConnectorDescription> model = new LoadableDetachableModel<ConnectorDescription>() {
            @Override
            protected ConnectorDescription load() {
                return serviceManager.getAttributeValues(serviceId);
            }
        };

        editor =
            new ConnectorServiceEditor("editor", serviceId, descriptor.getAttributes(), model,
                descriptor.getFormValidator());
        add(editor);
    }

    public ServiceEditorPanel getEditorPanel() {
        return editor.getServiceEditorPanel();
    }

    @Override
    public String getHeaderMenuItem() {
        return TestClient.class.getSimpleName();
    }

}
