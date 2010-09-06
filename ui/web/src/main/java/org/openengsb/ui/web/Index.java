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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.ui.web.service.DomainService;

@SuppressWarnings("serial")
public class Index extends BasePage {

    @SpringBean
    private DomainService domainService;

    public Index() {
        add(new ListView<DomainProvider>("domains", domainService.domains()) {

            @Override
            protected void populateItem(ListItem<DomainProvider> item) {
                item.add(new Label("domain.name", item.getModelObject().getName(item.getLocale())));
                item.add(new Label("domain.description", item.getModelObject().getDescription(item.getLocale())));
                item.add(new Label("domain.class", item.getModelObject().getDomainInterface().getName()));
            }
        });
    }

}
