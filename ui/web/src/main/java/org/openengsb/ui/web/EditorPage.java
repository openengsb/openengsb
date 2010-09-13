/**

 Copyright 2010 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.ui.web.editor.EditorPanel;

public class EditorPage extends BasePage {

    private final ServiceManager serviceManager;
    private EditorPanel editorPanel;

    public EditorPage(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        ServiceDescriptor descriptor = serviceManager.getDescriptor(getSession().getLocale());
        add(new Label("service.name", descriptor.getName()));
        add(new Label("service.description", descriptor.getDescription()));
        this.add(new BookmarkablePageLink<Index>("index", Index.class));
        createEditor( new HashMap<String, String>());
    }

    public EditorPage(ServiceManager serviceManager, String serviceId) {
        this.serviceManager = serviceManager;
        Map<String, String> attributeValues = serviceManager.getAttributeValues(serviceId);

        ServiceDescriptor descriptor = serviceManager.getDescriptor(getSession().getLocale());
        add(new Label("service.name", descriptor.getName()));
        add(new Label("service.description", descriptor.getDescription()));
        this.add(new BookmarkablePageLink<Index>("index", Index.class));
        createEditor(attributeValues);
    }


    @SuppressWarnings("serial")
    private void createEditor(Map<String, String> values) {
        List<AttributeDefinition> attributes = buildAttributeList(serviceManager);
        for (AttributeDefinition attribute : attributes) {
            if(!values.containsKey(attribute.getId())) { // do not overwrite attributes with default value
                values.put(attribute.getId(), attribute.getDefaultValue());
            }
        }
        editorPanel = new EditorPanel("editor", attributes, values) {
            @Override
            public void onSubmit() {
                serviceManager.update(getValues().get("id"), getValues());
                setResponsePage(TestClient.class);
            }
        };
        add(editorPanel);
    }

    private List<AttributeDefinition> buildAttributeList(ServiceManager service) {
        AttributeDefinition id = new AttributeDefinition();
        id.setId("id");
        id.setName(new StringResourceModel("attribute.id.name", this, null).getString());
        id.setDescription(new StringResourceModel("attribute.id.description", this, null).getString());
        id.setRequired(true);
        ServiceDescriptor descriptor = service.getDescriptor(getSession().getLocale());
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.add(id);
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

}
