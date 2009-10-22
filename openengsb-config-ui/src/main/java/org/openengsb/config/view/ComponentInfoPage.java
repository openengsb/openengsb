/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.view;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.openengsb.config.jbi.component.AttributeDescriptor;
import org.openengsb.config.jbi.component.ComponentDescriptor;
import org.openengsb.config.jbi.component.EndpointDescriptor;

public class ComponentInfoPage extends BasePage {
    public ComponentInfoPage(PageParameters params) {
        String name = params.getString("component");
        ComponentDescriptor desc = getComponent(name);
        setDefaultModel(new CompoundPropertyModel<ComponentDescriptor>(desc));
        add(new Label("name"));
        add(new Label("description"));
        add(new Label("targetNamespace"));
        add(new ListView<EndpointDescriptor>("endpoints", desc.getEndpoints()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<EndpointDescriptor> item) {
                item.add(new Label("endpoint.name", item.getModelObject().getName()));
                item.add(new ListView<AttributeDescriptor>("attributes", item.getModelObject().getAttributes()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void populateItem(ListItem<AttributeDescriptor> item) {
                        item.add(new Label("attribute.name", item.getModelObject().getName()));
                        item.add(new Label("attribute.type", item.getModelObject().getType().toString()));
                    }
                });
            }
        });
    }
}
