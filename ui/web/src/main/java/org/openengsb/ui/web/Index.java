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
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.Event;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.ui.web.service.DomainService;

@SuppressWarnings("serial")
public class Index extends BasePage {

    @SpringBean
    DomainService domainService;

    @SuppressWarnings("serial")
    public Index() {
        add(new Link<Object>("lang.en") {

            @Override
            public void onClick() {
                this.getSession().setLocale(Locale.ENGLISH);
            }
        });
        add(new Link<Object>("lang.de") {

            @Override
            public void onClick() {
                this.getSession().setLocale(Locale.GERMAN);
            }
        });
        add(new ListView<DomainProvider>("domains", domainService.domains()) {

            @Override
            protected void populateItem(ListItem<DomainProvider> item) {
                item.add(new Label("domain.name", item.getModelObject().getName(item.getLocale())));
                item.add(new Label("domain.description", item.getModelObject().getDescription(item.getLocale())));
                item.add(new Label("domain.class", item.getModelObject().getDomainInterface().getName()));
            }
        });
        List<ServiceManager> managers = new ArrayList<ServiceManager>(domainService.domains().size());
        for (DomainProvider provider : domainService.domains()) {
            managers.addAll(this.domainService.serviceManagersForDomain(provider.getDomainInterface()));
        }
        add(new ListView<ServiceManager>("services", managers) {

            @Override
            protected void populateItem(ListItem<ServiceManager> item) {
                ServiceDescriptor desc = item.getModelObject().getDescriptor(item.getLocale());
                item.add(new Link<ServiceManager>("create.new", item.getModel()) {

                    @Override
                    public void onClick() {
                        setResponsePage(new EditorPage(getModelObject()));
                    }
                });
                item.add(new Label("service.name", desc.getName()));
                item.add(new Label("service.description", desc.getDescription()));
            }
        });
        add(new BookmarkablePageLink<TestClient>("testclientlink", TestClient.class));
        add(new Link<SendEventPage>("sendEvent") {

            @Override
            public void onClick() {
                List<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>();
                events.add(Event.class);
                for (DomainProvider domain : domainService.domains()) {
                    events.addAll(domain.getEvents());
                }
                setResponsePage(new SendEventPage(events));
            }
        });

        add(new Link<ContextSetPage>("context.editor") {

            @Override
            public void onClick() {
                setResponsePage(new ContextSetPage());
            }
        });
    }
}
