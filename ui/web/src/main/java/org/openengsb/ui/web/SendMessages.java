package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

public class SendMessages extends BasePage {

    @SpringBean
    DomainService domainService;

    public SendMessages() {
        add(new ListView<DomainProvider>("domains", new ArrayList<DomainProvider>(domainService.domains())) {
            @Override
            protected void populateItem(ListItem<DomainProvider> item) {
                Class<? extends Domain> domainInterface = item.getModelObject().getDomainInterface();
                item.add(new Label("domainName", domainInterface.getName()));
                item.add(new ListView<ServiceManager>("connectors", domainService
                        .serviceManagersForDomain(domainInterface)) {
                    @Override
                    protected void populateItem(ListItem<ServiceManager> item) {
                        ServiceDescriptor descriptor = item.getModelObject().getDescriptor();
                        item.add(new Label("connectorName", descriptor.getName()));
                        item.add(new ListView<ServiceReference>("instances", domainService
                                .serviceReferencesForConnector(descriptor.getType())) {

                            @Override
                            protected void populateItem(final ListItem<ServiceReference> item) {
                                item.add(new Label("instanceName", item.getModelObject().getProperty("name").toString()));
                                item.add(new ListView<String>("properties", Arrays.asList(item.getModelObject()
                                        .getPropertyKeys())) {
                                    @Override
                                    protected void populateItem(ListItem<String> keyItem) {
                                        keyItem.add(new Label("key", keyItem.getModelObject()));
                                        keyItem.add(new Label("value", item.getModelObject()
                                                .getProperty(keyItem.getModelObject()).toString()));
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

    }
}