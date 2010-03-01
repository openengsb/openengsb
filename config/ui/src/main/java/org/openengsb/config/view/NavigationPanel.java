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

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.openengsb.config.jbi.types.ComponentType;

public class NavigationPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public NavigationPanel(String id, List<ComponentType> components) {
        super(id);
        PageParameters params = new PageParameters();
        params.put("reset", "1");
        add(new BookmarkablePageLink<ShowServiceAssemblyPage>("createAssemblyLink", ShowServiceAssemblyPage.class, params));
        add(new BookmarkablePageLink<EditContextPage>("editContextLink", EditContextPage.class));
        add(new ListView<ComponentType>("components", components) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<ComponentType> item) {
                item.setModel(new CompoundPropertyModel<ComponentType>(item.getModelObject()));
                final PageParameters params = new PageParameters();
                params.put("component", item.getModelObject().getName());
                item.add(new BookmarkablePageLink<ComponentInfoPage>("componentLink", ComponentInfoPage.class, params)
                        .add(new Label("name")));
            }
        });
    }
}
