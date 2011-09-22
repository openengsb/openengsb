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
import java.util.Map.Entry;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.security.SecurityAttribute;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.serviceEditor.ServiceEditor;
import org.openengsb.ui.admin.testClient.TestClient;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.openengsb.ui.common.model.LocalizableStringModel;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "CONNECTOR_EDITOR")
@PaxWicketMountPoint(mountPoint = "connectors/editor")
public class ConnectorEditorPage extends BasePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorEditorPage.class);

    @PaxWicketBean
    private ConnectorManager serviceManager;
    private ServiceDescriptor descriptor;
    private ServiceEditor editor;
    @PaxWicketBean
    private OsgiUtilsService serviceUtils;

    @SuppressWarnings("serial")
    private final class ConnectorServiceEditor extends ServiceEditor {

        private boolean createMode;
        private Map<String, String> attributeMap;

        private ConnectorServiceEditor(String id, String domainType, String connectorType,
                List<AttributeDefinition> attributes, Map<String, String> attributeMap,
                Map<String, Object> properties, FormValidator validator) {
            super(id, domainType, connectorType, attributes, attributeMap, properties, validator);
            createMode = true;
            this.attributeMap = attributeMap;
        }

        private ConnectorServiceEditor(String id, ConnectorId serviceId, List<AttributeDefinition> attributes,
                Map<String, String> attributeMap, Map<String, Object> properties, FormValidator validator) {
            super(id, serviceId, attributes, attributeMap, properties, validator);
            createMode = false;
            this.attributeMap = attributeMap;
        }

        @Override
        public void onSubmit() {
            ConnectorDescription connectorDescription = new ConnectorDescription(attributeMap, properties);
            try {
                if (createMode) {
                    if (isValidating()) {
                        serviceManager.create(idModel.getObject(), connectorDescription);
                    } else {
                        serviceManager.forceCreate(idModel.getObject(), connectorDescription);
                    }
                } else {
                    if (isValidating()) {
                        serviceManager.update(idModel.getObject(), connectorDescription);
                    } else {
                        serviceManager.forceUpdate(idModel.getObject(), connectorDescription);
                    }

                }
                returnToTestClient();
            } catch (ConnectorValidationFailedException e) {
                for (Entry<String, String> entry : e.getErrorMessages().entrySet()) {
                    error(String.format("%s: %s", entry.getKey(), entry.getValue()));
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Couldn't create service", e);
                error("The service already exists in the system. Please choose a different servcie id.");
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
        Filter filter =
            serviceUtils.makeFilter(ConnectorProvider.class,
                String.format("(%s=%s)", Constants.CONNECTOR_KEY, connectorType));

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
        ConnectorDescription description = new ConnectorDescription(values, null);
        editor =
            new ConnectorServiceEditor("editor", domainType, connectorType, attributes, description.getAttributes(),
                description.getProperties(), descriptor.getFormValidator());
        add(editor);
    }

    private void createEditor(final ConnectorId serviceId) {
        ConnectorDescription description = serviceManager.getAttributeValues(serviceId);

        editor =
            new ConnectorServiceEditor("editor", serviceId, descriptor.getAttributes(), description.getAttributes(),
                description.getProperties(), descriptor.getFormValidator());
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
