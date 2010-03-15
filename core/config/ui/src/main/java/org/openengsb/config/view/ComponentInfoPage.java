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
package org.openengsb.config.view;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;

public class ComponentInfoPage extends BasePage {

    @SuppressWarnings("serial")
    public ComponentInfoPage(PageParameters params) {
        final String name = params.getString("component");
        ComponentType desc = componentService.getComponent(name);
        setDefaultModel(new CompoundPropertyModel<ComponentType>(desc));
        add(new Label("name"));
        add(new Label("namespace"));
        add(new ListView<EndpointType>("endpoints") {

            @Override
            protected void populateItem(ListItem<EndpointType> item) {
                item.add(new Label("endpoint.name", item.getModelObject().getName()));
                final PageParameters pp = new PageParameters();
                pp.put("component", name);
                pp.put("endpoint", item.getModelObject().getName());
                item.add(new BookmarkablePageLink<BeanEditorPage>("editorLink", BeanEditorPage.class, pp).add(new Label("name", item.getModelObject().getName())) );
                item.add(new ListView<AbstractType>("attributes", item.getModelObject().getAttributes()) {

                    @Override
                    protected void populateItem(ListItem<AbstractType> item) {
                        item.add(new Label("attribute.name", item.getModelObject().getName()));
                        item.add(new Label("attribute.type", item.getModelObject().getClass().getName()));
                    }
                });
            }
        });
    }
}
