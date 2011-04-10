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

package org.openengsb.ui.admin.serviceListPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainService;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.LocalizableString;
import org.openengsb.core.api.l10n.PassThroughLocalizableString;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.common.model.LocalizableStringModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("serial")
public class ServiceListPanel extends Panel {

    private final DomainService services;

    private final BundleContext bundleContext;

    private final Map<AliveState, List<ServiceReference>> domainServiceMap;

    public ServiceListPanel(String id, BundleContext bundleContext, DomainService services,
            List<InternalServiceRegistrationManager> serviceManager) {
        super(id);
        this.bundleContext = bundleContext;
        this.services = services;
        domainServiceMap = new HashMap<AliveState, List<ServiceReference>>();
        domainServiceMap.put(AliveState.CONNECTING, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveState.DISCONNECTED, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveState.ONLINE, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveState.OFFLINE, new ArrayList<ServiceReference>());

        IModel<List<ServiceReference>> connectingServicesLoadableModel =
            createServiceReferenceModel(AliveState.CONNECTING);
        IModel<List<ServiceReference>> onlineServicesLoadableModel = createServiceReferenceModel(AliveState.ONLINE);
        IModel<List<ServiceReference>> offlineServicesLoadableModel = createServiceReferenceModel(AliveState.OFFLINE);
        IModel<List<ServiceReference>> disconnectedServicesLoadableModel =
            createServiceReferenceModel(AliveState.DISCONNECTED);

        WebMarkupContainer connectingServicePanel = new WebMarkupContainer("connectingServicePanel");
        connectingServicePanel.setOutputMarkupId(true);
        WebMarkupContainer onlineServicePanel = new WebMarkupContainer("onlineServicePanel");
        onlineServicePanel.setOutputMarkupId(true);
        WebMarkupContainer offlineServicePanel = new WebMarkupContainer("offlineServicePanel");
        offlineServicePanel.setOutputMarkupId(true);
        WebMarkupContainer disconnectedServicePanel = new WebMarkupContainer("disconnectedServicePanel");
        disconnectedServicePanel.setOutputMarkupId(true);

        Label noConServices =
            new Label("noConServices", new StringResourceModel("noServicesAvailable", this, null).getString());
        Label noOnServices =
            new Label("noOnServices", new StringResourceModel("noServicesAvailable", this, null).getString());
        Label noOffServices =
            new Label("noOffServices", new StringResourceModel("noServicesAvailable", this, null).getString());
        Label noDiscServices =
            new Label("noDisServices", new StringResourceModel("noServicesAvailable", this, null).getString());

        connectingServicePanel.add(createServiceListView(connectingServicesLoadableModel, "connectingServices",
            noConServices, connectingServicePanel));
        onlineServicePanel.add(createServiceListView(onlineServicesLoadableModel, "onlineServices", noOnServices,
            onlineServicePanel));
        offlineServicePanel.add(createServiceListView(offlineServicesLoadableModel, "offlineServices", noOffServices,
            offlineServicePanel));
        disconnectedServicePanel.add(createServiceListView(disconnectedServicesLoadableModel, "disconnectedServices",
            noDiscServices, disconnectedServicePanel));

        noConServices.setVisible(false);
        noOnServices.setVisible(false);
        noOffServices.setVisible(false);
        noDiscServices.setVisible(false);

        noConServices.setOutputMarkupId(true);
        noOnServices.setOutputMarkupId(true);
        noOffServices.setOutputMarkupId(true);
        noDiscServices.setOutputMarkupId(true);

        connectingServicePanel.add(noConServices);
        onlineServicePanel.add(noOnServices);
        offlineServicePanel.add(noOffServices);
        disconnectedServicePanel.add(noDiscServices);

        add(connectingServicePanel);
        add(onlineServicePanel);
        add(offlineServicePanel);
        add(disconnectedServicePanel);

        if (connectingServicesLoadableModel.getObject().isEmpty()) {
            noConServices.setVisible(true);
        }
        if (onlineServicesLoadableModel.getObject().isEmpty()) {
            noOnServices.setVisible(true);
        }
        if (offlineServicesLoadableModel.getObject().isEmpty()) {
            noOffServices.setVisible(true);
        }
        if (disconnectedServicesLoadableModel.getObject().isEmpty()) {
            noDiscServices.setVisible(true);
        }
    }

    private ListView<ServiceReference> createServiceListView(final IModel<List<ServiceReference>> serviceLoadableModel,
            final String id, final Label noServicesLabel, final WebMarkupContainer serviceWebMarkupContainer) {
        return new ListView<ServiceReference>(id, serviceLoadableModel) {

            @Override
            protected void populateItem(final ListItem<ServiceReference> item) {
                final ServiceReference serv = item.getModelObject();
                final String connector = (String) serv.getProperty("connector");
                final String domain = (String) serv.getProperty("domain");
                final String id = (String) serv.getProperty("id");
                final ConnectorId connectorId = new ConnectorId(domain, connector, id);
                LocalizableString description = new PassThroughLocalizableString("");
                InternalServiceRegistrationManager sm;
                try {
                    ServiceDescriptor desc = getServiceDescriptor(connector);
                    if (desc != null) {
                        description = desc.getDescription();
                    }
                } catch (OsgiServiceNotAvailableException e) {
                    error("could not find service-manager for connector " + connector);
                }
                item.add(new Label("service.name", id));
                item.add(new Label("service.description", new LocalizableStringModel(this, description)));
                item.add(new AjaxLink<String>("updateService", new Model<String>(connector)) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        InternalServiceRegistrationManager sm;
                        try {
                            sm = getServiceManager(getModelObject());
                        } catch (OsgiServiceNotAvailableException e) {
                            error(e);
                            return;
                        }
                        setResponsePage(new ConnectorEditorPage(connectorId));
                    }
                });
                item.add(new AjaxLink<String>("deleteService", new Model<String>(connector)) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        getList().remove(item.getModelObject());
                        InternalServiceRegistrationManager manager;
                        try {
                            manager = getServiceManager(getModelObject());
                        } catch (OsgiServiceNotAvailableException e) {
                            error(e);
                            return;
                        }
                        manager.delete(connectorId);
                        noServicesLabel.setVisible(getList().size() <= 0);
                        target.addComponent(noServicesLabel);
                        target.addComponent(serviceWebMarkupContainer);
                    }
                });
            }

            private InternalServiceRegistrationManager getServiceManager(String connector)
                throws OsgiServiceNotAvailableException {
                Filter filter;
                try {
                    filter =
                        OpenEngSBCoreServices.getServiceUtilsService().makeFilter(
                            InternalServiceRegistrationManager.class,
                            String.format("(connector=%s)", connector));
                } catch (InvalidSyntaxException e) {
                    throw new IllegalStateException(e);
                }
                return (InternalServiceRegistrationManager) OpenEngSBCoreServices.getServiceUtilsService().getService(
                    filter, 300);
            }
        };
    }

    protected ServiceDescriptor getServiceDescriptor(String connector) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressWarnings("serial")
    private LoadableDetachableModel<List<ServiceReference>> createServiceReferenceModel(final AliveState state) {
        return new LoadableDetachableModel<List<ServiceReference>>() {
            @Override
            protected List<ServiceReference> load() {
                updateDomainServiceMap();
                List<ServiceReference> managers = new ArrayList<ServiceReference>(services.domains().size());
                managers.addAll(domainServiceMap.get(state));
                return managers;
            }
        };
    }

    private void updateDomainServiceMap() {
        ServiceReference[] managedServiceInstances = getAllManagedServices();
        for (ServiceReference serviceReference : managedServiceInstances) {
            if (!"domain".equals(serviceReference.getProperty("openengsb.service.type"))) {
                Domain domainService = (Domain) services.getService(serviceReference);

                List<ServiceReference> serviceReferenceList = domainServiceMap.get(domainService.getAliveState());
                if (!serviceReferenceList.contains(serviceReference)) {
                    serviceReferenceList.add(serviceReference);
                }
            }
        }
    }

    private ServiceReference[] getAllManagedServices() {
        try {
            return bundleContext.getAllServiceReferences("org.openengsb.core.api.Domain", null);
        } catch (InvalidSyntaxException e) {
            String stackTrace = ExceptionUtils.getFullStackTrace(e.getCause());
            error(stackTrace);
            return new ServiceReference[0];
        }
    }

}
