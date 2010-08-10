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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.ui.web.editor.EditorPanel;

public class EditorPage extends BasePage {

    public EditorPage(ServiceManager service) {
        add(new Label("service.name", service.getDescriptor().getName()));
        add(new Label("service.description", service.getDescriptor().getDescription()));
        createEditor(service);
    }

    @SuppressWarnings("serial")
    private void createEditor(ServiceManager service) {
        List<AttributeDefinition> attributes = buildAttributeList(service);
        HashMap<String, String> values = new HashMap<String, String>();
        for (AttributeDefinition attribute : attributes) {
            values.put(attribute.getId(), attribute.getDefaultValue());
        }
        add(new EditorPanel("editor", attributes, values) {
            @Override
            public void onSubmit() {
            }
        });
    }

    private List<AttributeDefinition> buildAttributeList(ServiceManager service) {
        AttributeDefinition id = AttributeDefinition.builder()
                .id("id")
                .name(new StringResourceModel("attribute.id.name", this, null).getString())
                .description(new StringResourceModel("attribute.id.description", this, null).getString())
                .required()
                .build();
        ServiceDescriptor descriptor = service.getDescriptor(getSession().getLocale());
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.add(id);
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }
}
